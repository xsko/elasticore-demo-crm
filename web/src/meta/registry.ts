import type { EntityUi, FieldUi, Lang, LifecycleUi } from './types';

/**
 * Every entity's screen model and every enumeration, collected from the generated client
 * at build time. An entity added to the DSL appears here - and so in the menu, the lists,
 * the forms and the Excel export - with no change to the screens.
 */
const uiModules = import.meta.glob<{ default: EntityUi }>('../../../src/main/ts/*Ui.ts', { eager: true });
const allModules = import.meta.glob<Record<string, unknown>>('../../../src/main/ts/*.ts', { eager: true });

export const entities: Record<string, EntityUi> = {};
for (const m of Object.values(uiModules)) {
  const e = m.default;
  if (e && e.name) entities[e.name] = e;
}

/** Root entities with an API, in menu order (@ui(order=..) on the entity line, then name). */
export const roots: EntityUi[] = Object.values(entities)
  .filter((e) => e.root && e.path)
  .sort((a, b) => num(a.ui?.order) - num(b.ui?.order) || a.name.localeCompare(b.name));

function num(v: unknown): number {
  return typeof v === 'number' ? v : 999;
}

/**
 * An enumeration as ElastiCORE writes it for the browser: the constants object (NAME -> code,
 * in the DSL's order), XxxLabel (the model's own language) and XxxLabels (the other languages,
 * from the enum's label_xx columns).
 */
interface EnumInfo {
  values: string[];
  names: Record<string, string>;
  label: Record<string, string>;
  labels: Record<string, Record<string, string>>;
}

const enums: Record<string, EnumInfo> = {};
for (const mod of Object.values(allModules)) {
  for (const [exportName, value] of Object.entries(mod)) {
    if (!exportName.endsWith('Label') || !value || typeof value !== 'object') continue;
    const name = exportName.slice(0, -'Label'.length);
    const constants = mod[name] as Record<string, string> | undefined;
    if (!constants || typeof constants !== 'object') continue;
    const names: Record<string, string> = {};
    for (const [constant, code] of Object.entries(constants)) names[code] = constant;
    enums[name] = {
      values: Object.values(constants),
      names,
      label: value as Record<string, string>,
      labels: (mod[name + 'Labels'] as Record<string, Record<string, string>> | undefined) ?? {},
    };
  }
}

/**
 * One refusal of the DSL's constraints as ElastiCORE's validateXxxCodes reports it: the field,
 * the constraint's code (the same as the server's fieldErrors: NotNull, Size, Min, Max,
 * DecimalMin, DecimalMax, Pattern, Email) and its bounds.
 */
export interface CodeError {
  field: string;
  code: string;
  values: Record<string, number | string>;
}
type CodeValidator = (value: Record<string, unknown> | null | undefined, partial?: boolean) => CodeError[];

const validators: Record<string, CodeValidator> = {};
for (const mod of Object.values(allModules)) {
  for (const [exportName, value] of Object.entries(mod)) {
    const m = /^validate(\w+)Codes$/.exec(exportName);
    if (m && typeof value === 'function') validators[m[1]] = value as CodeValidator;
  }
}

/**
 * The DSL's constraints of an entity's request body, checked in the browser as the server
 * checks them; partial for an update that leaves fields out. None for an entity without a
 * generated validator.
 */
export function validateCodes(e: EntityUi, body: Record<string, unknown>, partial: boolean): CodeError[] {
  return validators[e.name]?.(body, partial) ?? [];
}

export function entityOf(name: string): EntityUi {
  const e = entities[name];
  if (!e) throw new Error(`No screen model for ${name}`);
  return e;
}

/** The resource name Refine routes by: the entity's API path segment (account, opportunity). */
export function resourceOf(e: EntityUi): string {
  return e.path ? e.path.substring(e.path.lastIndexOf('/') + 1) : e.name;
}

export function entityByResource(resource: string): EntityUi | undefined {
  return roots.find((e) => resourceOf(e) === resource) ?? entities[resource];
}

export function labelOf(x: { label: string; labels: Readonly<Record<string, string>> }, lang: Lang): string {
  if (lang === 'ko') return x.label;
  return x.labels[lang] ?? x.label;
}

export function enumValues(enumName: string): string[] {
  return enums[enumName]?.values ?? [];
}

export function enumLabel(enumName: string | undefined, value: unknown, lang: Lang): string {
  if (value == null || value === '') return '';
  const e = enumName ? enums[enumName] : undefined;
  if (!e) return String(value);
  const own = e.label[String(value)];
  if (lang === 'ko') return own ?? String(value);
  return e.labels[String(value)]?.[lang] ?? own ?? String(value);
}

/** The constant name of a wire value (PRS -> PROSPECT): what lifecycle moves are written in. */
export function enumName(enumName: string | undefined, value: unknown): string | undefined {
  if (!enumName || value == null) return undefined;
  return enums[enumName]?.names[String(value)];
}

/** The wire value of a constant name (PROSPECT -> PRS). */
export function enumCode(enumName: string | undefined, constant: string): string | undefined {
  if (!enumName) return undefined;
  const e = enums[enumName];
  if (!e) return undefined;
  return Object.entries(e.names).find(([, n]) => n === constant)?.[0];
}

export function field(e: EntityUi, name: string): FieldUi | undefined {
  return e.fields.find((f) => f.name === name);
}

/** The field that titles a row: @ui(title), else name, title, subject, else the key. */
export function titleField(e: EntityUi): string {
  const marked = e.fields.find((f) => f.ui?.title);
  if (marked) return marked.name;
  for (const n of ['name', 'title', 'subject']) if (e.fields.some((f) => f.name === n)) return n;
  return e.key ?? e.fields[0]?.name;
}

/** The lifecycle that a screen shows as the record's path (the first one). */
export function primaryLifecycle(e: EntityUi): (LifecycleUi & { enumName?: string }) | undefined {
  const lc = e.lifecycles[0];
  if (!lc) return undefined;
  return { ...lc, enumName: field(e, lc.field)?.enum };
}

/**
 * The colour a value of an enumeration is shown in, read from the shape of the lifecycle
 * that moves it - never from its name: the initial state is "new", the states it passes
 * through "step1", "step2"..., the end states (no way on but back to the start) "done"
 * for the first the DSL lists and "lost" for the others. An enumeration no lifecycle moves
 * (an industry, a priority) is "neutral".
 */
export function enumTone(enumName: string | undefined, value: unknown): string {
  if (!enumName || value == null) return 'neutral';
  if (!toneCache) toneCache = computeTones();
  return toneCache[enumName]?.[String(value)] ?? 'neutral';
}

let toneCache: Record<string, Record<string, string>> | null = null;

function computeTones(): Record<string, Record<string, string>> {
  const out: Record<string, Record<string, string>> = {};
  for (const e of Object.values(entities)) {
    for (const lc of e.lifecycles) {
      const en = field(e, lc.field)?.enum;
      if (!en || out[en]) continue;
      const names = enumValues(en).map((v) => enumName(en, v)!).filter(Boolean);
      const initial = lc.initial ?? names[0];
      const onward = (s: string) => lc.moves.filter((m) => m.from.includes(s) && m.to !== initial);
      const map: Record<string, string> = {};
      let step = 0;
      let ends = 0;
      for (const s of names) {
        const code = enumCode(en, s)!;
        if (s === initial) map[code] = 'new';
        else if (onward(s).length === 0) map[code] = ends++ === 0 ? 'done' : 'lost';
        else map[code] = `step${Math.min(++step, 3)}`;
      }
      out[en] = map;
    }
  }
  return out;
}

/** Fields a list shows: not hidden, not long text, not a nested list, not the shown-through identity. */
export function listFields(e: EntityUi): FieldUi[] {
  return e.fields.filter((f) =>
    !f.ui?.hidden && f.ui?.list !== false && f.kind !== 'text' && f.kind !== 'list' && f.kind !== 'json' && !f.display);
}

/** Fields a form edits on create or update. */
export function formFields(e: EntityUi, mode: 'create' | 'edit'): FieldUi[] {
  return e.fields.filter((f) => {
    if (f.ui?.hidden || f.display || f.derived || f.generated || f.key && f.generated) return false;
    if (f.readOnly) return false;
    if (mode === 'edit' && f.createOnly) return false;
    if (f.kind === 'list') return f.of != null && !!entities[f.of] && mode === 'create';
    return true;
  });
}
