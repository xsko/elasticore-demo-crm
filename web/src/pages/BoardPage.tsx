import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router';
import { App, Space, Spin } from 'antd';
import { NotFound } from '@/layout/Notices';
import { ViewSwitch } from './ListPage';
import { useTranslation } from 'react-i18next';
import { entityByResource, enumCode, enumLabel, enumName, enumTone, enumValues, labelOf, primaryLifecycle, resourceOf, titleField } from '@/meta/registry';
import { formatMoney, formatValue } from '@/meta/format';
import { Initials } from '@/grid/Cells';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { dataProvider } from '@/api/dataProvider';
import { allowed } from '@/api/accessControl';
import { signedInNow } from '@/api/authProvider';
import { api } from '@/api/http';

const PER_COLUMN = 50;

/**
 * Any entity with a lifecycle as a board: a column per state, in the order the DSL lists
 * them. Dropping a card on another column runs the command whose move the DSL declares
 * between the two states; a drop no move allows is refused before it reaches the server
 * (which checks again).
 */
export function BoardPage({ resource: fixed }: { resource?: string }) {
  const params = useParams();
  const entity = entityByResource(fixed ?? params.resource ?? '');
  if (!entity || !primaryLifecycle(entity)) return <NotFound />;
  return <Board key={entity.name} entity={entity} />;
}

function Board({ entity }: { entity: EntityUi }) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const { message } = App.useApp();
  const lc = primaryLifecycle(entity)!;
  const resource = resourceOf(entity);
  const states = enumValues(lc.enumName ?? '');
  const [columns, setColumns] = useState<Record<string, { rows: Record<string, unknown>[]; total: number }>>({});
  const [loading, setLoading] = useState(true);
  const [dragging, setDragging] = useState<Record<string, unknown> | null>(null);
  const who = signedInNow();

  const card = useMemo(() => cardFields(entity), [entity]);
  const title = titleField(entity);

  const load = async () => {
    setLoading(true);
    const next: typeof columns = {};
    await Promise.all(states.map(async (code) => {
      const res = await dataProvider.getList({
        resource, pagination: { currentPage: 1, pageSize: PER_COLUMN },
        filters: [{ field: lc.field, operator: 'eq', value: code }],
        sorters: entity.defaultSort ? [{ field: entity.defaultSort.replace(/[+-]$/, ''), order: entity.defaultSort.endsWith('-') ? 'desc' : 'asc' }] : [],
      });
      next[code] = { rows: res.data as Record<string, unknown>[], total: res.total };
    }));
    setColumns(next);
    setLoading(false);
  };
  useEffect(() => { load(); }, [entity.name]);   // eslint-disable-line react-hooks/exhaustive-deps

  const moveFor = (from: string, to: string) => {
    const f = enumName(lc.enumName, from);
    const tName = enumName(lc.enumName, to);
    return lc.moves.find((m) => m.to === tName && f != null && m.from.includes(f));
  };

  // Pointer events rather than the browser's drag and drop: they work with a mouse, a pen
  // and a finger alike. A press that moves less than 6 px is a click (the card's link).
  const press = useRef<{ row: Record<string, unknown>; x: number; y: number; moved: boolean } | null>(null);
  const [over, setOver] = useState<string | null>(null);
  const columnAt = (x: number, y: number) => (document.elementFromPoint(x, y)?.closest('[data-state]') as HTMLElement | null)?.dataset.state ?? null;
  const onDown = (e: React.PointerEvent, row: Record<string, unknown>) => {
    if (e.button !== 0) return;
    press.current = { row, x: e.clientX, y: e.clientY, moved: false };
    const move = (ev: PointerEvent) => {
      const p = press.current;
      if (!p) return;
      if (!p.moved && Math.hypot(ev.clientX - p.x, ev.clientY - p.y) < 6) return;
      if (!p.moved) { p.moved = true; setDragging(p.row); }
      setOver(columnAt(ev.clientX, ev.clientY));
    };
    const up = (ev: PointerEvent) => {
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', up);
      const p = press.current;
      press.current = null;
      setOver(null);
      if (p?.moved) {
        const to = columnAt(ev.clientX, ev.clientY);
        if (to) drop(p.row, to); else setDragging(null);
      }
    };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
  };

  const drop = async (row: Record<string, unknown>, to: string) => {
    setDragging(null);
    const from = String(row[lc.field]);
    if (from === to) return;
    const move = moveFor(from, to);
    if (!move || !allowed(entity, move.command, who)) {
      message.warning(t('pipeline.notAllowed', { from: enumLabel(lc.enumName, from, lang), to: enumLabel(lc.enumName, to, lang) }));
      return;
    }
    try {
      await api('POST', `${entity.path}/${row.id}/${move.command}`);
      message.success(t('record.moved', { stage: enumLabel(lc.enumName, to, lang) }));
      await load();
    } catch (e) {
      message.error((e as Error).message);
    }
  };

  const money = entity.fields.find((f) => f.kind === 'money');
  return (
    <div className="crm-page">
      <div className="crm-page-head">
        <div className="crm-page-title"><h1>{labelOf(entity, lang)}</h1></div>
        <Space size={12}><span className="crm-hint">{t('pipeline.hint')}</span><ViewSwitch resource={resource} current="board" /></Space>
      </div>
      <Spin spinning={loading}>
        <div className="crm-board">
          {states.map((code) => {
            const col = columns[code];
            const target = dragging ? moveFor(String(dragging[lc.field]), code) : undefined;
            const sum = money && col ? col.rows.reduce((s, r) => s + Number(r[money.name] ?? 0), 0) : undefined;
            return (
              <div key={code} data-state={code}
                   className={`crm-board-col${dragging ? (target ? ' can-drop' : String(dragging[lc.field]) === code ? '' : ' no-drop') : ''}${over === code && target ? ' over' : ''}`}>
                <div className={`crm-board-head tone-${enumTone(lc.enumName, code)}`}>
                  <span className="name">{enumLabel(lc.enumName, code, lang)}</span>
                  <span className="count">{col?.total ?? 0}</span>
                  {sum != null && money && <span className="sum">{formatMoney(sum, lang, true)}</span>}
                </div>
                <div className="crm-board-cards">
                  {col?.rows.map((r) => {
                    const val = (f: FieldUi) => (f.kind === 'ref' && f.ref?.nameField ? r[f.ref.nameField] : r[f.name]);
                    const refs = card.filter((f) => f.kind === 'ref' && f.ref?.idField !== entity.owner);
                    const amount = card.find((f) => f.kind === 'money');
                    const date = card.find((f) => f.kind === 'date');
                    const owner = card.find((f) => f.kind === 'ref' && f.ref?.idField === entity.owner);
                    return (
                      <div key={String(r.id)} className={`crm-card${dragging === r ? ' dragging' : ''}`}
                           onPointerDown={(e) => onDown(e, r)}>
                        <Link to={`/${resource}/show/${r.id}`} className="crm-card-title" draggable={false}
                              onClick={(e) => { if (dragging) e.preventDefault(); }}>{String(r[title] ?? `#${r.id}`)}</Link>
                        {refs.map((f) => (val(f) ? <div key={f.name} className="crm-card-line">{formatValue(f, val(f), lang, r)}</div> : null))}
                        <div className="crm-card-foot">
                          {amount && val(amount) != null && <span className="amount">{formatMoney(val(amount), lang)}</span>}
                          {date && val(date) ? <span className="date">{formatValue(date, val(date), lang)}</span> : null}
                          {owner && val(owner) ? <Initials name={String(val(owner))} size={22} /> : null}
                        </div>
                      </div>
                    );
                  })}
                  {col && col.total > col.rows.length && <div className="crm-board-more">+{col.total - col.rows.length}</div>}
                </div>
              </div>
            );
          })}
        </div>
      </Spin>
    </div>
  );
}

/** What a card shows besides its title: the first amount, the first reference names and the first date. */
function cardFields(e: EntityUi): FieldUi[] {
  const out: FieldUi[] = [];
  const pick = (pred: (f: FieldUi) => boolean, n = 1) => {
    for (const f of e.fields) if (out.length < 6 && pred(f) && !out.includes(f) && n-- > 0) out.push(f);
  };
  pick((f) => f.kind === 'money');
  pick((f) => f.kind === 'ref' && !!f.ref?.nameField && f.ref.idField !== e.owner, 2);
  pick((f) => f.kind === 'date' && !f.readOnly);
  pick((f) => f.kind === 'ref' && !!f.ref?.nameField && f.ref.idField === e.owner);
  return out;
}

export { enumCode };
