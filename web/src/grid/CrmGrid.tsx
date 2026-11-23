import { useMemo, useRef, useState, type ReactNode } from 'react';
import { App, Button, Checkbox, Dropdown, Pagination, Popover, Space, Table, Tooltip } from 'antd';
import type { ColumnsType, TableProps } from 'antd/es/table';
import { useTable, useDelete, type CrudFilter, type CrudSort, type BaseRecord } from '@refinedev/core';
import { keepPreviousData } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ArrowDown, ArrowUp, Columns3, FileSpreadsheet, RefreshCw, Rows3, Rows4, Trash2 } from 'lucide-react';
import { labelOf, listFields, resourceOf, titleField } from '@/meta/registry';
import { isNumeric, sortKey } from '@/meta/format';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { allowed, seesOnlyOwn } from '@/api/accessControl';
import { signedInNow } from '@/api/authProvider';
import { Cell } from './Cells';
import { FilterBar } from './FilterBar';
import { clearPrefs, loadPrefs, savePrefs, type ColumnPrefs } from './columnPrefs';
import { defaultWidth, exportAll, exportShown } from './excel';

const PAGE_SIZES = [20, 50, 100];
const EXPORT_MAX = 100000;

/**
 * The list of an entity: a real HTML table (Ant Design Table) over the generated search
 * API. Paging, sorting and searching happen on the server; the page, page size, sort and
 * search live in the address (a reload or a shared link shows the same list). Columns
 * come from the DSL; which are shown, their order, width and the row height are each
 * user's own. Excel: the table as shown (in the browser) or every matching row (the
 * server, through the same access checks).
 */
export function CrmGrid({ entity, permanentFilters, toolbarExtra, compactHeader }: {
  entity: EntityUi;
  permanentFilters?: CrudFilter[];
  toolbarExtra?: ReactNode;
  compactHeader?: boolean;
}) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const { message, modal } = App.useApp();
  const resource = resourceOf(entity);
  // A member who only ever sees their own rows does not need a column with their own name.
  const ownOnly = seesOnlyOwn(entity, signedInNow());
  const fields = useMemo(() => listFields(entity).filter((f) => !(ownOnly && f.kind === 'ref' && f.ref?.idField === entity.owner)),
    [entity, ownOnly]);
  const title = titleField(entity);
  const [prefs, setPrefs] = useState<ColumnPrefs>(() => loadPrefs(entity.name, fields.map((f) => f.name)));
  const [selected, setSelected] = useState<React.Key[]>([]);
  const [busy, setBusy] = useState(false);
  const { mutateAsync: remove } = useDelete();

  const initialSort: CrudSort[] = (entity.defaultSort ?? '')
    .split(',').filter(Boolean)
    .map((s) => ({ field: s.replace(/[+-]$/, ''), order: s.endsWith('-') ? 'desc' : 'asc' }));

  const table = useTable<BaseRecord>({
    resource,
    syncWithLocation: !permanentFilters,
    pagination: { pageSize: 20 },
    sorters: { initial: initialSort },
    filters: { permanent: permanentFilters ?? [], defaultBehavior: 'replace' },
    queryOptions: { placeholderData: keepPreviousData },
  });
  const rows = (table.result.data ?? []) as Record<string, unknown>[];
  const total = table.result.total ?? 0;

  const update = (p: ColumnPrefs) => {
    setPrefs(p);
    savePrefs(entity.name, p);
  };

  const shown: FieldUi[] = prefs.order
    .map((n) => fields.find((f) => f.name === n)!)
    .filter((f) => f && !prefs.hidden.includes(f.name));

  const sortOf = (f: FieldUi) => {
    const s = table.sorters.find((x) => x.field === sortKey(f));
    return s ? (s.order === 'desc' ? 'descend' : 'ascend') : null;
  };

  const columns: ColumnsType<Record<string, unknown>> = shown.map((f) => ({
    key: f.name,
    dataIndex: f.name,
    title: labelOf(f, lang),
    width: prefs.widths[f.name] ?? defaultWidth(f),
    align: isNumeric(f) ? 'right' : 'left',
    ellipsis: true,
    sorter: f.kind !== 'list' && f.kind !== 'embedded' && !f.encrypted,
    sortOrder: sortOf(f),
    fixed: f.name === title ? 'left' : undefined,
    onHeaderCell: () => ({ width: prefs.widths[f.name] ?? defaultWidth(f), onResize: (w: number) => update({ ...prefs, widths: { ...prefs.widths, [f.name]: w } }) }) as never,
    render: (_: unknown, row: Record<string, unknown>) => (
      <Cell f={f} row={row} lang={lang} titleLink={f.name === title ? `/${resource}/show/${row.id}` : undefined} />
    ),
  }));

  const onChange: TableProps<Record<string, unknown>>['onChange'] = (_p, _f, sorter) => {
    const list = (Array.isArray(sorter) ? sorter : [sorter]).filter((s) => s.order && s.columnKey);
    const next: CrudSort[] = list.map((s) => {
      const f = fields.find((x) => x.name === s.columnKey)!;
      return { field: sortKey(f), order: s.order === 'descend' ? 'desc' : 'asc' };
    });
    table.setSorters(next.length ? next : initialSort);
  };

  const canDelete = allowed(entity, 'delete', signedInNow());

  const deleteSelected = () => {
    modal.confirm({
      title: t('common.confirmDelete', { count: selected.length }),
      okText: t('common.delete'),
      okButtonProps: { danger: true },
      cancelText: t('common.cancel'),
      onOk: async () => {
        for (const id of selected) await remove({ resource, id: id as string, successNotification: false });
        setSelected([]);
        message.success(t('common.deleted'));
      },
    });
  };

  const exportMenu = {
    items: [
      { key: 'view', label: <MenuLine title={t('excel.view')} hint={t('excel.viewHint')} /> },
      ...(selected.length ? [{ key: 'selection', label: <MenuLine title={t('excel.selection')} hint={t('common.selected', { count: selected.length })} /> }] : []),
      { key: 'all', label: <MenuLine title={t('excel.all')} hint={t('excel.allHint', { max: EXPORT_MAX.toLocaleString() })} /> },
    ],
    onClick: async ({ key }: { key: string }) => {
      setBusy(true);
      try {
        if (key === 'all') {
          if (total > EXPORT_MAX) {
            message.warning(t('excel.tooMany', { count: total, max: EXPORT_MAX }));
            return;
          }
          await exportAll(entity, shown, table.filters, table.sorters, lang);
          message.success(t('excel.done', { count: total }));
        } else {
          const list = key === 'selection' ? rows.filter((r) => selected.includes(r.id as React.Key)) : rows;
          await exportShown(entity, shown, prefs.widths, list, lang);
          message.success(t('excel.done', { count: list.length }));
        }
      } catch (e) {
        message.error((e as Error).message || t('common.error'));
      } finally {
        setBusy(false);
      }
    },
  };

  const from = total === 0 ? 0 : (table.currentPage - 1) * table.pageSize + 1;
  const to = Math.min(total, table.currentPage * table.pageSize);

  return (
    <div className="crm-grid">
      <FilterBar entity={entity} filters={table.filters.filter((f) => !permanentFilters?.includes(f))} onChange={(f) => table.setFilters(f, 'replace')} />
      <div className={compactHeader ? 'crm-grid-toolbar compact' : 'crm-grid-toolbar'}>
        <div className="crm-grid-count">
          {selected.length > 0 ? (
            <Space size={8}>
              <strong>{t('common.selected', { count: selected.length })}</strong>
              {canDelete && <Button size="small" danger icon={<Trash2 size={13} />} onClick={deleteSelected}>{t('common.delete')}</Button>}
              <Button size="small" type="text" onClick={() => setSelected([])}>{t('common.cancel')}</Button>
            </Space>
          ) : (
            <span>{t('common.total', { count: total.toLocaleString() as unknown as number })}</span>
          )}
        </div>
        <Space size={4}>
          {toolbarExtra}
          <Tooltip title={t('common.refresh')}>
            <Button size="small" type="text" icon={<RefreshCw size={14} className={table.tableQuery.isFetching ? 'spin' : ''} />} onClick={() => table.tableQuery.refetch()} />
          </Tooltip>
          <Tooltip title={t('common.density')}>
            <Button size="small" type="text" icon={prefs.dense ? <Rows4 size={14} /> : <Rows3 size={14} />} onClick={() => update({ ...prefs, dense: !prefs.dense })} />
          </Tooltip>
          <Popover trigger="click" placement="bottomRight" content={
            <ColumnChooser fields={fields} prefs={prefs} lang={lang} onChange={update}
                           onReset={() => { clearPrefs(entity.name); setPrefs(loadPrefs(entity.name, fields.map((f) => f.name))); }} />
          }>
            <Tooltip title={t('common.columns')}><Button size="small" type="text" icon={<Columns3 size={14} />} /></Tooltip>
          </Popover>
          <Dropdown menu={exportMenu} trigger={['click']} placement="bottomRight">
            <Button size="small" icon={<FileSpreadsheet size={14} />} loading={busy}>{t('excel.menu')}</Button>
          </Dropdown>
        </Space>
      </div>
      <Table<Record<string, unknown>>
        rowKey="id"
        size={prefs.dense ? 'small' : 'middle'}
        columns={columns}
        dataSource={rows}
        loading={table.tableQuery.isLoading}
        pagination={false}
        onChange={onChange}
        scroll={{ x: 'max-content' }}
        sticky
        showSorterTooltip={false}
        components={{ header: { cell: ResizableTitle } }}
        rowSelection={{ selectedRowKeys: selected, onChange: setSelected, columnWidth: 36, preserveSelectedRowKeys: true }}
        locale={{ emptyText: <div className="crm-empty">{t('common.noData')}</div> }}
      />
      <div className="crm-grid-footer">
        <span className="crm-grid-range">{t('common.range', { from: from.toLocaleString(), to: to.toLocaleString(), total: total.toLocaleString() })}</span>
        <Pagination
          size="small"
          current={table.currentPage}
          pageSize={table.pageSize}
          total={total}
          showSizeChanger
          pageSizeOptions={PAGE_SIZES}
          locale={{ items_per_page: lang === 'ko' ? '건씩' : '/ page' }}
          onChange={(page, size) => {
            if (size !== table.pageSize) {
              table.setPageSize(size);
              table.setCurrentPage(1);
            } else {
              table.setCurrentPage(page);
            }
          }}
        />
      </div>
    </div>
  );
}

function MenuLine({ title, hint }: { title: string; hint: string }) {
  return (
    <div className="crm-menu-line">
      <div>{title}</div>
      <div className="hint">{hint}</div>
    </div>
  );
}

function ColumnChooser({ fields, prefs, lang, onChange, onReset }: {
  fields: FieldUi[]; prefs: ColumnPrefs; lang: Lang; onChange: (p: ColumnPrefs) => void; onReset: () => void;
}) {
  const { t } = useTranslation();
  const move = (i: number, d: number) => {
    const order = [...prefs.order];
    const j = i + d;
    if (j < 0 || j >= order.length) return;
    [order[i], order[j]] = [order[j], order[i]];
    onChange({ ...prefs, order });
  };
  return (
    <div className="crm-columns">
      <div className="crm-columns-head">{t('common.showColumns')}</div>
      {prefs.order.map((n, i) => {
        const f = fields.find((x) => x.name === n);
        if (!f) return null;
        const on = !prefs.hidden.includes(n);
        return (
          <div key={n} className="crm-columns-row">
            <Checkbox checked={on} onChange={(e) => onChange({ ...prefs, hidden: e.target.checked ? prefs.hidden.filter((x) => x !== n) : [...prefs.hidden, n] })}>
              {labelOf(f, lang)}
            </Checkbox>
            <span>
              <Button size="small" type="text" icon={<ArrowUp size={12} />} onClick={() => move(i, -1)} disabled={i === 0} />
              <Button size="small" type="text" icon={<ArrowDown size={12} />} onClick={() => move(i, 1)} disabled={i === prefs.order.length - 1} />
            </span>
          </div>
        );
      })}
      <div className="crm-columns-foot"><Button size="small" type="link" onClick={onReset}>{t('common.resetColumns')}</Button></div>
    </div>
  );
}

/** A header cell whose right edge drags the column wider or narrower. */
function ResizableTitle(props: React.ThHTMLAttributes<HTMLTableCellElement> & { onResize?: (w: number) => void; width?: number }) {
  const { onResize, width, children, ...rest } = props;
  const start = useRef<{ x: number; w: number } | null>(null);
  if (!onResize || !width) return <th {...rest}>{children}</th>;
  const down = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    start.current = { x: e.clientX, w: width };
    const moveTo = (ev: MouseEvent) => {
      if (!start.current) return;
      onResize(Math.max(60, Math.round(start.current.w + ev.clientX - start.current.x)));
    };
    const up = () => {
      start.current = null;
      window.removeEventListener('mousemove', moveTo);
      window.removeEventListener('mouseup', up);
    };
    window.addEventListener('mousemove', moveTo);
    window.addEventListener('mouseup', up);
  };
  return (
    <th {...rest} style={{ ...rest.style, position: 'relative' }}>
      {children}
      <span className="crm-col-resize" onMouseDown={down} onClick={(e) => e.stopPropagation()} />
    </th>
  );
}
