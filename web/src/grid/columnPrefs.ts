/** A user's column choices for one entity's list: which, in what order, how wide, how dense. */
export interface ColumnPrefs {
  order: string[];
  hidden: string[];
  widths: Record<string, number>;
  dense: boolean;
}

const key = (entity: string) => `grid:${entity}`;

export function loadPrefs(entity: string, defaults: string[]): ColumnPrefs {
  const base: ColumnPrefs = { order: defaults, hidden: [], widths: {}, dense: true };
  try {
    const raw = localStorage.getItem(key(entity));
    if (!raw) return base;
    const p = JSON.parse(raw) as Partial<ColumnPrefs>;
    // A field added to the DSL since the choice was saved appears at the end; one removed drops out.
    const known = new Set(defaults);
    const order = (p.order ?? []).filter((n) => known.has(n));
    for (const n of defaults) if (!order.includes(n)) order.push(n);
    return { order, hidden: (p.hidden ?? []).filter((n) => known.has(n)), widths: p.widths ?? {}, dense: p.dense ?? true };
  } catch {
    return base;
  }
}

export function savePrefs(entity: string, prefs: ColumnPrefs): void {
  try {
    localStorage.setItem(key(entity), JSON.stringify(prefs));
  } catch {
    /* storage may be unavailable */
  }
}

export function clearPrefs(entity: string): void {
  try {
    localStorage.removeItem(key(entity));
  } catch {
    /* ignore */
  }
}
