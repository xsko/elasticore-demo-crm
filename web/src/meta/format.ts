import dayjs from 'dayjs';
import type { FieldUi, Lang } from './types';
import { enumLabel } from './registry';
import { appInfo } from './app';

const locales: Record<Lang, string> = { ko: 'ko-KR', en: 'en-US' };

/** An amount in the installation's currency: $431,000 / ₩431,000,000; compact: $18.6M / ₩186억. */
export function formatMoney(value: unknown, lang: Lang, compact = false): string {
  if (value == null || value === '') return '';
  const n = Number(value);
  return new Intl.NumberFormat(locales[lang], {
    style: 'currency', currency: appInfo.currency, maximumFractionDigits: compact ? 1 : 0,
    ...(compact ? { notation: 'compact' as const } : {}),
  }).format(n);
}

/** A value as text, the same on the screen and in the Excel file. */
export function formatValue(f: FieldUi, value: unknown, lang: Lang, row?: Record<string, unknown>): string {
  if (value == null || value === '') {
    if (f.kind === 'ref' && row && f.ref?.idField && row[f.ref.idField] != null) return `#${row[f.ref.idField]}`;
    return '';
  }
  switch (f.kind) {
    case 'enum':
      return enumLabel(f.enum, value, lang);
    case 'money':
      return formatMoney(value, lang);
    case 'decimal':
      return new Intl.NumberFormat(locales[lang], { maximumFractionDigits: 4 }).format(Number(value));
    case 'integer':
    case 'quantity':
      return f.key ? String(value) : new Intl.NumberFormat(locales[lang]).format(Number(value));
    case 'boolean':
      return value === true || value === 'true' ? (lang === 'ko' ? '예' : 'Yes') : (lang === 'ko' ? '아니요' : 'No');
    case 'date':
      return dayjs(String(value)).format('YYYY-MM-DD');
    case 'datetime':
      return dayjs(String(value).replace(' ', 'T')).format('YYYY-MM-DD HH:mm');
    case 'timestamp':
      return dayjs(String(value)).format('YYYY-MM-DD HH:mm');
    default:
      return String(value);
  }
}

/** The value of a reference field: the name the answer carries (@ref), else its identity. */
export function refValue(f: FieldUi, row: Record<string, unknown>): unknown {
  if (f.kind !== 'ref') return row[f.name];
  if (f.ref?.nameField && row[f.ref.nameField] != null) return row[f.ref.nameField];
  return f.ref?.idField ? row[f.ref.idField] : row[f.name];
}

/** The key a list sorts a column by: a reference sorts by its identity column. */
export function sortKey(f: FieldUi): string {
  if (f.kind === 'ref') return f.ref?.idField ?? f.name;
  return f.name;
}

export function isNumeric(f: FieldUi): boolean {
  return (f.kind === 'money' || f.kind === 'decimal' || f.kind === 'integer' || f.kind === 'quantity') && !f.key;
}
