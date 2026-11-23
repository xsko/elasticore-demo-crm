import type { CrudFilter, CrudSort } from '@refinedev/core';
import { labelOf } from '@/meta/registry';
import { formatValue, isNumeric, refValue } from '@/meta/format';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { searchOf } from '@/api/dataProvider';
import { commonHeaders, toHttpError, xsrfToken } from '@/api/http';
import dayjs from 'dayjs';

/** A cell that starts with = + - @ would run as a formula in Excel: keep it text. */
export function safeText(s: string): string {
  return /^[=+\-@\t\r]/.test(s) ? `'${s}` : s;
}

const XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

export function fileName(e: EntityUi, lang: Lang): string {
  return `${labelOf(e, lang)}_${dayjs().format('YYYYMMDD-HHmm')}.xlsx`;
}

/**
 * The table as the user sees it, in the browser: the visible columns in their order and
 * width, the rows of this page (or the selected ones), labels and values as shown.
 */
export async function exportShown(e: EntityUi, fields: FieldUi[], widths: Record<string, number>,
                                  rows: Record<string, unknown>[], lang: Lang): Promise<void> {
  const ExcelJS = (await import('exceljs')).default;
  const wb = new ExcelJS.Workbook();
  wb.creator = 'ElastiCORE CRM';
  const ws = wb.addWorksheet(sheetName(labelOf(e, lang)), { views: [{ state: 'frozen', ySplit: 1 }] });
  ws.columns = fields.map((f) => ({
    header: labelOf(f, lang),
    key: f.name,
    width: Math.max(10, Math.round((widths[f.name] ?? defaultWidth(f)) / 7)),
  }));
  for (const row of rows) {
    const values: Record<string, unknown> = {};
    for (const f of fields) values[f.name] = cellValue(f, row, lang);
    ws.addRow(values);
  }
  fields.forEach((f, i) => {
    const col = ws.getColumn(i + 1);
    if (f.kind === 'money') col.numFmt = '#,##0';
    else if (f.kind === 'decimal') col.numFmt = '#,##0.####';
    else if (isNumeric(f)) col.numFmt = '#,##0';
    else if (f.kind === 'date') col.numFmt = 'yyyy-mm-dd';
    else if (f.kind === 'datetime' || f.kind === 'timestamp') col.numFmt = 'yyyy-mm-dd hh:mm';
  });
  const header = ws.getRow(1);
  header.font = { bold: true };
  header.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF1F3F6' } };
  header.border = { bottom: { style: 'thin', color: { argb: 'FFC9CED6' } } };
  ws.autoFilter = { from: { row: 1, column: 1 }, to: { row: 1, column: fields.length } };
  const buffer = await wb.xlsx.writeBuffer();
  download(new Blob([buffer], { type: XLSX }), fileName(e, lang));
}

/**
 * Every row of the search, written by the server: the export ElastiCORE generates for an
 * entity whose @expose lists it (POST <path>/export). It pages through the same search the
 * table makes - its access check, @owner filter and @Mask - so the file holds exactly what
 * this user may see, in the table's columns, order and titles. More rows than the server's
 * elasticore.export.max-rows is a 400 before anything is written.
 */
export async function exportAll(e: EntityUi, fields: FieldUi[], filters: CrudFilter[], sorters: CrudSort[],
                                lang: Lang): Promise<void> {
  const body = {
    search: searchOf(e, filters),
    sortCode: sorters.map((s) => s.field + (s.order === 'desc' ? '-' : '+')).join(',') || undefined,
    // A reference is exported by the name the answer carries (accountName), else its key.
    columns: fields.map((f) => (f.kind === 'ref' ? f.ref?.nameField ?? f.ref?.idField ?? f.name : f.name)),
    headers: fields.map((f) => labelOf(f, lang)),
    // Every enumeration column by its label in the request's language (발굴 / Prospecting), not its code.
    labels: '*',
    format: 'xlsx',
  };
  if (!xsrfToken()) await fetch('/api/crm/member/me', { credentials: 'same-origin' });
  const res = await fetch(`${e.path}/export`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', Accept: XLSX, ...commonHeaders() },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let problem: unknown;
    try {
      problem = await res.json();
    } catch {
      problem = undefined;
    }
    throw toHttpError(res.status, problem);
  }
  download(await res.blob(), fileName(e, lang));
}

function cellValue(f: FieldUi, row: Record<string, unknown>, lang: Lang): unknown {
  const v = refValue(f, row);
  if (v == null || v === '') return null;
  if (isNumeric(f)) return Number(v);
  if (f.kind === 'date') return dayjs(String(v)).toDate();
  if (f.kind === 'datetime') return dayjs(String(v).replace(' ', 'T')).toDate();
  if (f.kind === 'timestamp') return dayjs(String(v)).toDate();
  return safeText(formatValue(f, v, lang, row));
}

export function defaultWidth(f: FieldUi): number {
  if (typeof f.ui?.width === 'number') return f.ui.width;
  switch (f.kind) {
    case 'boolean': return 80;
    case 'date': return 110;
    case 'datetime':
    case 'timestamp': return 150;
    case 'money':
    case 'decimal': return 130;
    case 'integer':
    case 'quantity': return f.key ? 90 : 100;
    case 'enum': return 120;
    case 'ref': return 170;
    default: return f.maxLength && f.maxLength <= 30 ? 140 : 200;
  }
}

function sheetName(s: string): string {
  return s.replace(/[\\/?*[\]:]/g, ' ').slice(0, 31) || 'Sheet1';
}

function download(blob: Blob, name: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
