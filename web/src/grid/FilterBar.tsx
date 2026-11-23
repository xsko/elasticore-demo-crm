import { useEffect, useMemo, useRef, useState } from 'react';
import { Button, DatePicker, Input, InputNumber, Select } from 'antd';
import type { CrudFilter } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { Search, X } from 'lucide-react';
import dayjs, { type Dayjs } from 'dayjs';
import { enumLabel, enumValues, labelOf } from '@/meta/registry';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { RefSelect } from '@/form/RefSelect';

/**
 * The search row of a list: one input per field the DSL marks @search, shaped by its
 * operator - a text box for like, a range for between, a list for an enumeration, a
 * lookup for a reference. Typing waits 300 ms before it asks the server.
 */
export function FilterBar({ entity, filters, onChange }: {
  entity: EntityUi;
  filters: CrudFilter[];
  onChange: (filters: CrudFilter[]) => void;
}) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const searchable = useMemo(
    () => entity.fields.filter((f) => f.search && !f.key && !f.ui?.hidden && f.kind !== 'text' && !(f.encrypted && f.search !== 'eq')),
    [entity],
  );
  const [values, setValues] = useState<Record<string, unknown>>(() => fromFilters(filters));
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

  useEffect(() => { setValues(fromFilters(filters)); }, [entity.name]);   // eslint-disable-line react-hooks/exhaustive-deps

  const push = (next: Record<string, unknown>, wait: number) => {
    setValues(next);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => onChange(toFilters(searchable, next)), wait);
  };

  if (searchable.length === 0) return null;
  const set = (name: string, v: unknown, wait = 0) => push({ ...values, [name]: v }, wait);

  return (
    <div className="crm-filterbar">
      {searchable.map((f) => (
        <div key={f.name} className="crm-filter">
          <label>{labelOf(f, lang)}</label>
          {control(f, values[f.name], (v, wait) => set(f.name, v, wait), lang)}
        </div>
      ))}
      <Button type="text" size="small" icon={<X size={14} />} onClick={() => push({}, 0)} className="crm-filter-reset">
        {t('common.reset')}
      </Button>
    </div>
  );
}

function control(f: FieldUi, value: unknown, set: (v: unknown, wait?: number) => void, lang: Lang) {
  if (f.kind === 'enum' && f.enum) {
    return (
      <Select allowClear size="small" style={{ width: 140 }} value={value as string | undefined}
              options={enumValues(f.enum).map((v) => ({ value: v, label: enumLabel(f.enum, v, lang) }))}
              onChange={(v) => set(v)} />
    );
  }
  if (f.kind === 'ref' && f.ref) {
    return <RefSelect size="small" entity={f.ref.entity} value={value as number | undefined} onChange={(v) => set(v)} width={170} />;
  }
  if (f.kind === 'boolean') {
    return (
      <Select allowClear size="small" style={{ width: 90 }} value={value as string | undefined}
              options={[{ value: 'true', label: lang === 'ko' ? '예' : 'Yes' }, { value: 'false', label: lang === 'ko' ? '아니요' : 'No' }]}
              onChange={(v) => set(v)} />
    );
  }
  if (f.search === 'between' && (f.kind === 'date' || f.kind === 'datetime' || f.kind === 'timestamp')) {
    const range = (value as [string?, string?] | undefined) ?? [];
    return (
      <DatePicker.RangePicker size="small" style={{ width: 230 }}
        value={[range[0] ? dayjs(range[0]) : null, range[1] ? dayjs(range[1]) : null] as [Dayjs | null, Dayjs | null]}
        onChange={(d) => set(d ? [d[0] ? asParam(f, d[0], false) : undefined, d[1] ? asParam(f, d[1], true) : undefined] : undefined)} />
    );
  }
  if (f.search === 'between') {
    const range = (value as [number?, number?] | undefined) ?? [];
    return (
      <span className="crm-range">
        <InputNumber size="small" style={{ width: 100 }} value={range[0]} onChange={(v) => set([v ?? undefined, range[1]], 400)} />
        <span>~</span>
        <InputNumber size="small" style={{ width: 100 }} value={range[1]} onChange={(v) => set([range[0], v ?? undefined], 400)} />
      </span>
    );
  }
  return (
    <Input size="small" allowClear style={{ width: 160 }} prefix={<Search size={12} color="#8a93a0" />}
           value={(value as string | undefined) ?? ''} onChange={(e) => set(e.target.value, 300)} />
  );
}

function asParam(f: FieldUi, d: Dayjs, end: boolean): string {
  if (f.kind === 'date') return d.format('YYYY-MM-DD');
  const t = end ? d.endOf('day') : d.startOf('day');
  return f.kind === 'timestamp' ? t.toISOString() : t.format('YYYY-MM-DD HH:mm:ss');
}

function fromFilters(filters: CrudFilter[]): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const f of filters) if ('field' in f) out[f.field] = f.value;
  return out;
}

function toFilters(fields: FieldUi[], values: Record<string, unknown>): CrudFilter[] {
  const out: CrudFilter[] = [];
  for (const f of fields) {
    const v = values[f.name];
    if (v == null || v === '' || (Array.isArray(v) && v.every((x) => x == null || x === ''))) continue;
    out.push({ field: f.name, operator: f.search === 'between' ? 'between' : f.search === 'like' ? 'contains' : 'eq', value: v });
  }
  return out;
}
