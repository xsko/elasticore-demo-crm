import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { App, Button, Dropdown, Empty, Skeleton, Tabs } from 'antd';
import { NotFound } from '@/layout/Notices';
import { useCustomMutation, useDelete, useOne, useInvalidate } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { Check, ChevronRight, MoreHorizontal, Pencil, Trash2 } from 'lucide-react';
import dayjs from 'dayjs';
import { entities, entityByResource, enumCode, enumLabel, enumName, enumValues, labelOf, primaryLifecycle, resourceOf, titleField } from '@/meta/registry';
import { formatValue, refValue } from '@/meta/format';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { allowed } from '@/api/accessControl';
import { signedInNow } from '@/api/authProvider';
import { dataProvider } from '@/api/dataProvider';
import { Cell, StatusBadge } from '@/grid/Cells';
import { CrmGrid } from '@/grid/CrmGrid';
import { EntityForm } from '@/form/EntityForm';
import { EntityIcon } from '@/layout/icons';

/**
 * One record: a header with what identifies it and the moves its lifecycle allows now (each
 * a command the server checks again), the path through the states the DSL declares, its
 * fields, the lists of what points to it (an account's contacts, deals and activities) and
 * a timeline of the dated records about it - all from the screen model, for every entity.
 */
export function RecordPage() {
  const { resource = '', id = '' } = useParams();
  const entity = entityByResource(resource);
  if (!entity) return <NotFound />;
  return <Record entity={entity} id={id} />;
}

function Record({ entity, id }: { entity: EntityUi; id: string }) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const navigate = useNavigate();
  const { message, modal } = App.useApp();
  const resource = resourceOf(entity);
  const invalidate = useInvalidate();
  const { query, result } = useOne({ resource, id });
  const row = result as Record<string, unknown> | undefined;
  const [editing, setEditing] = useState(false);
  const { mutateAsync: run, mutation } = useCustomMutation();
  const { mutateAsync: remove } = useDelete();
  const who = signedInNow();

  const title = titleField(entity);
  const lifecycle = primaryLifecycle(entity);
  const related = useMemo(() => referencing(entity), [entity]);
  const timeline = useMemo(() => related.find(({ child }) => datedField(child)), [related]);

  if (query.isLoading) return <div className="crm-page"><Skeleton active /></div>;
  if (!row) return <NotFound extra={<Button onClick={() => navigate(`/${resource}`)}>{t('common.back')}</Button>} />;

  const highlights = entity.fields
    .filter((f) => f.name !== title && !f.key && !f.ui?.hidden && !f.display && f.kind !== 'text' && f.kind !== 'list' && f.name !== lifecycle?.field && !f.readOnly)
    .slice(0, 5);

  const current = lifecycle ? enumName(lifecycle.enumName, row[lifecycle.field]) : undefined;
  const moves = lifecycle ? lifecycle.moves.filter((m) => current && m.from.includes(current)) : [];
  const deletable = !lifecycle?.deletableIn || (current != null && lifecycle.deletableIn.includes(current));

  const step = async (command: string, to: string) => {
    try {
      await run({ url: `${resource}/${id}/${command}`, method: 'post', values: {}, successNotification: false, errorNotification: false });
      await invalidate({ resource, invalidates: ['all'] });
      message.success(t('record.moved', { stage: enumLabel(lifecycle?.enumName, enumCode(lifecycle?.enumName, to), lang) }));
    } catch (e) {
      message.error((e as Error).message);
    }
  };

  const del = () => modal.confirm({
    title: t('common.confirmDeleteOne'), okText: t('common.delete'), okButtonProps: { danger: true }, cancelText: t('common.cancel'),
    onOk: async () => {
      await remove({ resource, id, successNotification: false });
      message.success(t('common.deleted'));
      navigate(`/${resource}`);
    },
  });

  const canEdit = (entity.editable || entity.fields.some((f) => !f.readOnly && !f.createOnly && !f.key && !f.display && !f.derived)) && allowed(entity, 'update', who);
  const canDelete = allowed(entity, 'delete', who) && deletable;
  const shownMoves = moves.filter((m) => allowed(entity, m.command, who));
  const name = String(row[title] ?? `#${id}`);
  const showSide = !!timeline && allowed(timeline.child, 'read', who);

  return (
    <div className="crm-page crm-record">
      <div className="crm-record-head">
        <nav className="crm-crumbs">
          <Link to={`/${resource}`}>{labelOf(entity, lang)}</Link>
          <ChevronRight size={13} />
          <span>{name}</span>
        </nav>
        <div className="crm-record-title">
          <span className="crm-record-icon"><EntityIcon name={String(entity.ui?.icon ?? '')} size={20} /></span>
          <div className="crm-record-name">
            <h1>{name}</h1>
            {lifecycle && <StatusBadge enumName={lifecycle.enumName} value={row[lifecycle.field]} lang={lang} />}
          </div>
          <div className="crm-record-actions">
            {shownMoves.map((m, i) => (
              <Button key={m.command} type={i === 0 ? 'primary' : 'default'} loading={mutation.isPending}
                      onClick={() => step(m.command, m.to)}>
                {t(`commands.${m.command}`, { defaultValue: enumLabel(lifecycle!.enumName, enumCode(lifecycle!.enumName, m.to), lang) })}
              </Button>
            ))}
            {canEdit && <Button icon={<Pencil size={14} />} onClick={() => setEditing(true)}>{t('common.edit')}</Button>}
            {canDelete && (
              <Dropdown trigger={['click']} placement="bottomRight" menu={{ items: [
                { key: 'delete', danger: true, icon: <Trash2 size={14} />, label: t('common.delete'), onClick: del },
              ] }}>
                <Button icon={<MoreHorizontal size={16} />} aria-label={t('common.more')} />
              </Dropdown>
            )}
          </div>
        </div>
        <div className="crm-highlights">
          {highlights.map((f) => (
            <div key={f.name} className="crm-highlight">
              <div className="label">{labelOf(f, lang)}</div>
              <div className="value"><Cell f={f} row={row} lang={lang} /></div>
            </div>
          ))}
        </div>
        {lifecycle && (
          <div className="crm-path" role="list">
            {pathStates(lifecycle).map((s, i) => {
              const code = enumCode(lifecycle.enumName, s);
              const done = current != null && order(lifecycle, s) < order(lifecycle, current) && !isTerminalOther(lifecycle, current, s);
              return (
                <div key={s} role="listitem" className={`crm-path-step${s === current ? ' current' : ''}${done ? ' done' : ''}`} data-step={i + 1}>
                  {enumLabel(lifecycle.enumName, code, lang)}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div className={showSide ? 'crm-record-layout' : 'crm-record-layout single'}>
        <div className="crm-record-main crm-panel">
          <Tabs
            items={[
              { key: 'details', label: t('common.details'), children: <DetailPanel entity={entity} row={row} lang={lang} withSystem={!showSide} /> },
              ...related.map(({ child, via }) => ({
                key: `${child.name}.${via.name}`,
                label: labelOf(child, lang),
                children: allowed(child, 'read', who)
                  ? <CrmGrid entity={child} compactHeader permanentFilters={[{ field: via.name, operator: 'eq', value: Number(id) }]} />
                  : <Empty description={t('common.noAccess')} />,
              })),
            ]}
          />
        </div>
        {showSide && timeline && (
          <aside className="crm-record-side">
            <Timeline child={timeline.child} via={timeline.via} id={id} lang={lang} />
            <SystemCard entity={entity} row={row} lang={lang} />
          </aside>
        )}
      </div>

      <EntityForm entity={entity} open={editing} record={row} onClose={() => setEditing(false)}
                  onSaved={() => invalidate({ resource, invalidates: ['all'] })} />
    </div>
  );
}

/** The fields people fill in, in two columns (a long text across both), then the lines of an aggregate. */
function DetailPanel({ entity, row, lang, withSystem }: { entity: EntityUi; row: Record<string, unknown>; lang: Lang; withSystem: boolean }) {
  const visible = entity.fields.filter((f) => !f.ui?.hidden && !f.display && f.kind !== 'list');
  const main = visible.filter((f) => !isSystem(f));
  const system = visible.filter(isSystem);
  const lines = entity.fields.find((f) => f.kind === 'list' && f.of && entities[f.of]);
  return (
    <div className="crm-detail">
      <dl className="crm-fields">
        {main.map((f) => (
          <div key={f.name} className={f.kind === 'text' ? 'crm-field wide' : 'crm-field'}>
            <dt>{labelOf(f, lang)}</dt>
            <dd>{f.kind === 'text'
              ? <div className="crm-pre">{formatValue(f, row[f.name], lang) || <span className="crm-none">-</span>}</div>
              : (formatValue(f, refValue(f, row), lang, row) ? <Cell f={f} row={row} lang={lang} /> : <span className="crm-none">-</span>)}</dd>
          </div>
        ))}
      </dl>
      {lines && Array.isArray(row[lines.name]) && <MemberTable field={lines} rows={row[lines.name] as Record<string, unknown>[]} lang={lang} />}
      {withSystem && system.length > 0 && (
        <div className="crm-system-line">
          {system.map((f) => (
            <span key={f.name}>{labelOf(f, lang)} <b>{formatValue(f, refValue(f, row), lang, row) || '-'}</b></span>
          ))}
        </div>
      )}
    </div>
  );
}

function isSystem(f: FieldUi): boolean {
  return !!(f.key || f.derived || (f.readOnly && f.kind !== 'enum'));
}

/** When the record was made and moved: its read-only times, on the side. */
function SystemCard({ entity, row, lang }: { entity: EntityUi; row: Record<string, unknown>; lang: Lang }) {
  const { t } = useTranslation();
  const fields = entity.fields.filter((f) => isSystem(f) && !f.ui?.hidden && !f.display && f.kind !== 'list');
  if (!fields.length) return null;
  return (
    <section className="crm-panel crm-side-card">
      <h3>{t('common.system')}</h3>
      <dl className="crm-side-facts">
        {fields.map((f) => (
          <div key={f.name}><dt>{labelOf(f, lang)}</dt><dd>{formatValue(f, refValue(f, row), lang, row) || '-'}</dd></div>
        ))}
      </dl>
    </section>
  );
}

/** The dated records that point to this one (a deal's calls and meetings), newest first. */
function Timeline({ child, via, id, lang }: { child: EntityUi; via: FieldUi; id: string; lang: Lang }) {
  const { t } = useTranslation();
  const when = datedField(child)!;
  const [rows, setRows] = useState<Record<string, unknown>[] | null>(null);
  const [total, setTotal] = useState(0);
  useEffect(() => {
    dataProvider.getList({ resource: resourceOf(child), pagination: { currentPage: 1, pageSize: 8 },
      filters: [{ field: via.name, operator: 'eq', value: Number(id) }], sorters: [{ field: when.name, order: 'desc' }] })
      .then((r) => { setRows(r.data as Record<string, unknown>[]); setTotal(r.total); }).catch(() => setRows([]));
  }, [child, via, id, when.name]);
  const title = titleField(child);
  const kind = child.fields.find((f) => f.kind === 'enum' && !child.lifecycles.some((l) => l.field === f.name));
  const flag = child.fields.find((f) => f.kind === 'boolean');
  return (
    <section className="crm-panel crm-side-card">
      <h3>{labelOf(child, lang)} <span className="crm-count">{total}</span></h3>
      {rows == null ? <Skeleton active paragraph={{ rows: 3 }} /> : rows.length === 0 ? <div className="crm-none">{t('common.noData')}</div> : (
        <ol className="crm-timeline">
          {rows.map((r) => {
            const at = dayjs(String(r[when.name]).replace(' ', 'T'));
            const doneNow = flag ? r[flag.name] === true : at.isBefore(dayjs());
            return (
              <li key={String(r.id)} className={doneNow ? 'past' : 'next'}>
                <span className="crm-timeline-dot">{doneNow && <Check size={10} strokeWidth={3} />}</span>
                <div>
                  <Link to={`/${resourceOf(child)}/show/${r.id}`}>{String(r[title] ?? `#${r.id}`)}</Link>
                  <div className="meta">{at.locale(lang).format(lang === 'ko' ? 'M월 D일 HH:mm' : 'MMM D, HH:mm')}{kind ? ` · ${formatValue(kind, r[kind.name], lang)}` : ''}</div>
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </section>
  );
}

/** The date-time a child is planned by (an activity's due time): one people set, not a stamp. */
function datedField(e: EntityUi): FieldUi | undefined {
  return e.fields.find((f) => (f.kind === 'datetime' || f.kind === 'timestamp') && !f.readOnly && !f.derived);
}

function MemberTable({ field, rows, lang }: { field: FieldUi; rows: Record<string, unknown>[]; lang: Lang }) {
  const member = entities[field.of!];
  const cols = member.fields.filter((f) => !f.key && f.kind !== 'list');
  const display = (f: FieldUi) => member.fields.find((x) => x.display && f.kind === 'ref' && x.name.startsWith(f.name));
  return (
    <div className="crm-members">
      <div className="crm-section-title">{labelOf(field, lang)}</div>
      <table className="crm-mini-table bordered">
        <thead><tr>{cols.map((c) => <th key={c.name} className={c.kind === 'money' || c.kind === 'quantity' ? 'num' : ''}>{labelOf(c, lang)}</th>)}</tr></thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i}>
              {cols.map((c) => {
                const d = display(c);
                const v = d && r[d.name] != null ? String(r[d.name]) : formatValue(c, refValue(c, r), lang, r);
                return <td key={c.name} className={c.kind === 'money' || c.kind === 'quantity' ? 'num' : ''}>{v}</td>;
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/** Entities with a reference to this one: their lists are this record's related lists. */
function referencing(e: EntityUi): { child: EntityUi; via: FieldUi }[] {
  const out: { child: EntityUi; via: FieldUi }[] = [];
  for (const child of Object.values(entities)) {
    if (!child.root || !child.path || child.ui?.admin) continue;
    for (const f of child.fields) if (f.kind === 'ref' && f.ref?.entity === e.name && f.ref.idField !== child.owner) out.push({ child, via: f });
  }
  return out;
}

/** The states in the order of their enumeration, the initial one first. */
function pathStates(lc: ReturnType<typeof primaryLifecycle> & object): string[] {
  const names = enumValues(lc.enumName ?? '').map((v) => enumName(lc.enumName, v)!).filter(Boolean);
  return names.length ? names : Array.from(new Set([lc.initial ?? '', ...lc.moves.map((m) => m.to)])).filter(Boolean);
}

function order(lc: ReturnType<typeof primaryLifecycle> & object, state: string): number {
  return pathStates(lc).indexOf(state);
}

/** Of two end states (won, lost) the one not reached is not "done". */
function isTerminalOther(lc: ReturnType<typeof primaryLifecycle> & object, current: string, s: string): boolean {
  const outgoing = (x: string) => lc.moves.some((m) => m.from.includes(x));
  return !outgoing(s) && s !== current;
}
