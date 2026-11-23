import { useEffect, useState } from 'react';
import { Select } from 'antd';
import { entities, titleField } from '@/meta/registry';
import { dataProvider } from '@/api/dataProvider';
import { resourceOf } from '@/meta/registry';

/**
 * A lookup for a reference field: searches the target entity by its title field (the
 * generated search, so the user sees only what they may read) and stores its identity.
 */
export function RefSelect({ entity, value, onChange, width, size, disabled }: {
  entity: string;
  value?: number | string;
  onChange?: (v: number | string | undefined) => void;
  width?: number | string;
  size?: 'small' | 'middle';
  disabled?: boolean;
}) {
  const target = entities[entity];
  const title = target ? titleField(target) : 'name';
  const [options, setOptions] = useState<{ value: number | string; label: string }[]>([]);
  const [loading, setLoading] = useState(false);

  const load = async (text?: string) => {
    if (!target?.path) return;
    setLoading(true);
    try {
      const filters = text ? [{ field: title, operator: 'contains' as const, value: text }] : [];
      const res = await dataProvider.getList({ resource: resourceOf(target), pagination: { currentPage: 1, pageSize: 20 }, filters });
      setOptions(res.data.map((r) => ({ value: r[target.key!] as number, label: String(r[title] ?? r[target.key!]) })));
    } catch {
      setOptions([]);
    } finally {
      setLoading(false);
    }
  };

  // Make sure the current value is shown by its name.
  useEffect(() => {
    if (value == null || !target?.path || options.some((o) => o.value === value)) return;
    dataProvider.getOne({ resource: resourceOf(target), id: value })
      .then((r) => setOptions((o) => [...o, { value, label: String((r.data as Record<string, unknown>)[title] ?? value) }]))
      .catch(() => undefined);
  }, [value]);   // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <Select
      showSearch allowClear size={size} disabled={disabled} style={{ width: width ?? '100%' }}
      value={value} options={options} loading={loading} filterOption={false}
      onSearch={(t) => load(t)} onFocus={() => options.length < 2 && load()}
      onChange={(v) => onChange?.(v ?? undefined)}
    />
  );
}
