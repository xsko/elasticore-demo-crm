import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Button, Empty, Skeleton } from 'antd';
import { useGetIdentity } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { ArrowUpRight, Bot, Columns3 } from 'lucide-react';
import dayjs from 'dayjs';
import { entities, enumLabel, enumTone, enumValues, enumName as enumNameOf, field, labelOf, resourceOf, titleField } from '@/meta/registry';
import { formatMoney, formatValue } from '@/meta/format';
import type { EntityUi, Lang } from '@/meta/types';
import type { Me } from '@/api/authProvider';
import { api } from '@/api/http';
import { dataProvider } from '@/api/dataProvider';
import { Initials, StatusBadge } from '@/grid/Cells';

interface MetricValue { metric: string; description: string; value: number }
interface Bucket { key: string; value: number }

const WANTED = ['pipelineAmount', 'openDeals', 'wonAmount', 'wonDeals', 'closedDeals', 'winRate', 'openTickets', 'urgentTickets'];

/**
 * The sales home: the DSL's metrics (worked out on the server for the member who asks -
 * a rep sees their own book, the administrator everyone's), the pipeline by stage, and
 * what is due next. A tile whose metric is not in the model is not shown.
 */
export function HomePage() {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const { data: who } = useGetIdentity<Me>();
  const [kpis, setKpis] = useState<Record<string, number>>({});
  const [stages, setStages] = useState<Bucket[]>([]);
  const [counts, setCounts] = useState<Bucket[]>([]);
  const [loaded, setLoaded] = useState(false);

  const deals = entities.Opportunity;
  const stageField = deals?.lifecycles[0]?.field;
  const stageEnum = deals && stageField ? field(deals, stageField)?.enum : undefined;

  useEffect(() => {
    (async () => {
      const out: Record<string, number> = {};
      await Promise.all(WANTED.map(async (m) => {
        try {
          out[m] = Number((await api<MetricValue>('GET', `/api/crm/metrics/${m}`)).value);
        } catch {
          /* not in the model, or not this member's to see */
        }
      }));
      setKpis(out);
      if (stageField) {
        try {
          const order = (b: Bucket) => enumValues(stageEnum ?? '').indexOf(String(b.key));
          setStages((await api<Bucket[]>('GET', `/api/crm/metrics/pipelineAmount?by=${stageField}`)).sort((a, b) => order(a) - order(b)));
          setCounts(await api<Bucket[]>('GET', `/api/crm/metrics/openDeals?by=${stageField}`));
        } catch {
          setStages([]);
        }
      }
      setLoaded(true);
    })();
  }, [stageField]);   // eslint-disable-line react-hooks/exhaustive-deps

  const n = (v?: number) => (v == null ? '' : new Intl.NumberFormat(lang === 'ko' ? 'ko-KR' : 'en-US').format(v));
  const tiles = [
    { key: 'pipelineAmount', label: t('home.pipeline'), value: kpis.pipelineAmount, text: formatMoney(kpis.pipelineAmount ?? 0, lang, true),
      sub: kpis.openDeals != null ? t('home.deals', { count: kpis.openDeals }) : '' },
    { key: 'wonAmount', label: t('home.won'), value: kpis.wonAmount, text: formatMoney(kpis.wonAmount ?? 0, lang, true),
      sub: kpis.wonDeals != null ? t('home.wonDeals', { count: kpis.wonDeals }) : '' },
    { key: 'winRate', label: t('home.winRate'), value: kpis.winRate, text: `${((kpis.winRate ?? 0) * 100).toFixed(1)}%`,
      sub: kpis.wonDeals != null && kpis.closedDeals != null ? t('home.winRateSub', { won: n(kpis.wonDeals), closed: n(kpis.closedDeals) }) : '' },
    { key: 'openTickets', label: t('home.openTickets'), value: kpis.openTickets, text: n(kpis.openTickets ?? 0),
      sub: kpis.urgentTickets != null ? t('home.urgent', { count: kpis.urgentTickets }) : '', alert: (kpis.urgentTickets ?? 0) > 0 },
  ].filter((x) => x.value != null);

  const shown = stages.filter((s) => Number(s.value) > 0 || counts.some((c) => c.key === s.key && Number(c.value) > 0));
  const max = Math.max(1, ...shown.map((s) => Number(s.value)));
  const firstName = who?.name ? (lang === 'en' ? who.name.split(/\s+/)[0] : who.name) : '';
  const hour = dayjs().hour();
  const greeting = t(hour < 12 ? 'home.morning' : hour < 18 ? 'home.afternoon' : 'home.evening', { name: firstName });

  return (
    <div className="crm-page crm-home">
      <div className="crm-home-head">
        <div>
          <div className="crm-eyebrow">{dayjs().locale(lang).format(lang === 'ko' ? 'YYYY년 M월 D일 dddd' : 'dddd, MMMM D, YYYY')}</div>
          <h1>{greeting}</h1>
          <p className="crm-subtitle">{who?.role === 'A' ? t('home.subtitleAll') : t('home.subtitleMine')}</p>
        </div>
        <div className="crm-home-actions">
          <Link to="/assistant"><Button icon={<Bot size={15} />}>{t('menu.assistant')}</Button></Link>
          <Link to="/pipeline"><Button type="primary" icon={<Columns3 size={15} />}>{t('menu.pipeline')}</Button></Link>
        </div>
      </div>

      <div className="crm-kpis">
        {!loaded ? [0, 1, 2, 3].map((i) => <div key={i} className="crm-kpi"><Skeleton active title={false} paragraph={{ rows: 2 }} /></div>) : tiles.map((x) => (
          <div key={x.key} className="crm-kpi">
            <div className="label">{x.label}</div>
            <div className="value">{x.text}</div>
            {x.sub && <div className={`sub${x.alert ? ' alert' : ''}`}>{x.sub}</div>}
          </div>
        ))}
      </div>

      <div className="crm-home-grid">
        <div className="crm-home-col">
            <section className="crm-panel crm-card-panel wide">
              <header>
                <div><h3>{t('home.byStage')}</h3>
                  <span className="crm-panel-sub">{t('home.byStageSub', { amount: formatMoney(kpis.pipelineAmount ?? 0, lang), count: kpis.openDeals ?? 0 })}</span></div>
                <Link to="/pipeline" className="crm-more">{t('menu.pipeline')} <ArrowUpRight size={13} /></Link>
              </header>
              <div className="crm-stage-chart">
                {!loaded && <Skeleton active title={false} paragraph={{ rows: 3 }} />}
                {shown.map((s) => {
                  const count = counts.find((c) => c.key === s.key)?.value ?? 0;
                  return (
                    <div key={s.key} className="crm-stage-row" title={`${enumLabel(stageEnum, s.key, lang)}: ${formatMoney(s.value, lang)}`}>
                      <span className="crm-stage-name"><span className={`crm-dot tone-${enumTone(stageEnum, s.key)}`} />{enumLabel(stageEnum, s.key, lang)}</span>
                      <span className="crm-stage-track"><span className={`crm-stage-bar tone-${enumTone(stageEnum, s.key)}`} style={{ width: `${Math.max(2, (Number(s.value) / max) * 100)}%` }} /></span>
                      <span className="crm-stage-amount">{formatMoney(s.value, lang, true)}</span>
                      <span className="crm-stage-count">{t('home.deals', { count: Number(count) })}</span>
                    </div>
                  );
                })}
                {loaded && shown.length === 0 && <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('common.noData')} />}
              </div>
            </section>
            <ClosingList />
        </div>
        <div className="crm-home-col">
            <UpcomingList />
            <RecentList entityName="Ticket" />
        </div>
      </div>
    </div>
  );
}

function PanelHead({ title, to, link }: { title: string; to?: string; link?: string }) {
  return (
    <header>
      <h3>{title}</h3>
      {to && <Link to={to} className="crm-more">{link} <ArrowUpRight size={13} /></Link>}
    </header>
  );
}

function UpcomingList() {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const e = entities.Activity;
  const [rows, setRows] = useState<Record<string, unknown>[] | null>(null);
  useEffect(() => {
    if (!e) return;
    dataProvider.getList({ resource: resourceOf(e), pagination: { currentPage: 1, pageSize: 6 },
      filters: [{ field: 'done', operator: 'eq', value: false }, { field: 'dueAt', operator: 'gte', value: dayjs().format('YYYY-MM-DD HH:mm:ss') }],
      sorters: [{ field: 'dueAt', order: 'asc' }] })
      .then((r) => setRows(r.data as Record<string, unknown>[])).catch(() => setRows([]));
  }, [e]);
  if (!e) return null;
  const type = field(e, 'type');
  return (
    <section className="crm-panel crm-card-panel">
      <PanelHead title={t('home.upcoming')} to={`/${resourceOf(e)}`} link={labelOf(e, lang)} />
      {rows == null ? <Skeleton active /> : rows.length === 0 ? <Empty description={t('home.noUpcoming')} image={Empty.PRESENTED_IMAGE_SIMPLE} /> : (
        <ul className="crm-agenda">
          {rows.map((r) => {
            const due = dayjs(String(r.dueAt).replace(' ', 'T'));
            return (
              <li key={String(r.id)}>
                <span className="crm-date-chip">
                  <span className="m">{due.locale(lang).format(lang === 'ko' ? 'M월' : 'MMM')}</span>
                  <span className="d">{due.format('D')}</span>
                </span>
                <span className="what">
                  <Link to={`/${resourceOf(e)}/show/${r.id}`}>{String(r[titleField(e)])}</Link>
                  <span className="meta">{due.format('HH:mm')} · {type ? formatValue(type, r.type, lang) : ''}{r.accountName ? ` · ${String(r.accountName)}` : ''}</span>
                </span>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}

function ClosingList() {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const e = entities.Opportunity;
  const [rows, setRows] = useState<Record<string, unknown>[] | null>(null);
  useEffect(() => {
    if (!e) return;
    const start = dayjs().startOf('month');
    const end = start.add(3, 'month').subtract(1, 'day');
    dataProvider.getList({ resource: resourceOf(e), pagination: { currentPage: 1, pageSize: 20 },
      filters: [{ field: 'expectedCloseDate', operator: 'between', value: [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')] }],
      sorters: [{ field: 'amount', order: 'desc' }] })
      .then((r) => setRows((r.data as Record<string, unknown>[]).filter((x) => isOpen(e, x)).slice(0, 7))).catch(() => setRows([]));
  }, [e]);
  if (!e) return null;
  const stage = field(e, 'stage');
  const close = field(e, 'expectedCloseDate');
  return (
    <section className="crm-panel crm-card-panel wide">
      <PanelHead title={t('home.closing')} to={`/${resourceOf(e)}`} link={labelOf(e, lang)} />
      {rows == null ? <Skeleton active /> : rows.length === 0 ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('common.noData')} /> : (
        <table className="crm-mini-table">
          <tbody>
            {rows.map((r) => (
              <tr key={String(r.id)}>
                <td><Link to={`/${resourceOf(e)}/show/${r.id}`} className="crm-title-link">{String(r.name)}</Link><div className="meta">{String(r.accountName ?? '')}</div></td>
                <td><StatusBadge enumName={stage?.enum} value={r.stage} lang={lang} /></td>
                <td className="meta-cell">{close ? formatValue(close, r.expectedCloseDate, lang) : ''}</td>
                <td className="num strong">{formatMoney(r.amount, lang)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function RecentList({ entityName }: { entityName: string }) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const e: EntityUi | undefined = entities[entityName];
  const [rows, setRows] = useState<Record<string, unknown>[] | null>(null);
  useEffect(() => {
    if (!e) return;
    dataProvider.getList({ resource: resourceOf(e), pagination: { currentPage: 1, pageSize: 6 } })
      .then((r) => setRows(r.data as Record<string, unknown>[])).catch(() => setRows([]));
  }, [e]);
  if (!e) return null;
  const status = e.lifecycles[0] ? field(e, e.lifecycles[0].field) : undefined;
  return (
    <section className="crm-panel crm-card-panel">
      <PanelHead title={t('home.recentTickets')} to={`/${resourceOf(e)}`} link={labelOf(e, lang)} />
      {rows == null ? <Skeleton active /> : rows.length === 0 ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('common.noData')} /> : (
        <ul className="crm-feed">
          {rows.map((r) => (
            <li key={String(r.id)}>
              <Initials name={String(r.customerName ?? '')} size={26} />
              <span className="what"><Link to={`/${resourceOf(e)}/show/${r.id}`}>{String(r[titleField(e)])}</Link>
                <span className="meta">{String(r.customerName ?? '')}</span></span>
              {status && <StatusBadge enumName={status.enum} value={r[status.name]} lang={lang} />}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

/** Still in play: its state can go on along at least two moves (a closed deal has one or none). */
function isOpen(e: EntityUi, row: Record<string, unknown>): boolean {
  const lc = e.lifecycles[0];
  if (!lc) return true;
  const name = enumNameOf(field(e, lc.field)?.enum, row[lc.field]);
  return lc.moves.filter((m) => name != null && m.from.includes(name)).length >= 2;
}
