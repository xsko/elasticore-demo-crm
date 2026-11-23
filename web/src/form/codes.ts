import type { TFunction } from 'i18next';
import { entities, formFields, labelOf, validateCodes, type CodeError } from '@/meta/registry';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';

/**
 * The DSL's constraints, checked in the browser before a request is sent. ElastiCORE's
 * generated validateXxxCodes reports each refusal by the code the server's fieldErrors carry
 * (NotNull, Size, Min, Max, Pattern, Email ...); the screen says it in its own words, in the
 * chosen language. No field's bounds are written in the screens.
 */
export function codeMessage(err: CodeError, label: string, value: unknown, t: TFunction): string {
  const v = err.values;
  switch (err.code) {
    case 'NotNull':
    case 'NotBlank':
    case 'NotEmpty':
      return t('common.required', { label });
    case 'Size': {
      const n = Array.isArray(value) ? value.length : String(value ?? '').length;
      if (v.max != null && n > Number(v.max)) return t('common.maxLength', { label, n: v.max });
      return t(Array.isArray(value) ? 'validation.minItems' : 'validation.minLength', { label, n: v.min });
    }
    case 'Min':
    case 'DecimalMin':
      return t('validation.min', { label, n: v.value });
    case 'Max':
    case 'DecimalMax':
      return t('validation.max', { label, n: v.value });
    case 'Pattern':
      return t('validation.pattern', { label });
    case 'Email':
      return t('validation.email', { label });
    default:
      return t('validation.invalid', { label });
  }
}

export interface FormFieldError {
  name: string | (string | number)[];
  errors: string[];
}

/**
 * The form errors of a request: the body's own fields, and each row of a list of members (a
 * quote's lines), checked by the member's own validator. rows are the list's form rows, so a
 * message lands on the row the person typed; a row left empty is not sent, so not checked.
 */
export function formErrors(
  entity: EntityUi,
  fields: FieldUi[],
  body: Record<string, unknown>,
  rows: Record<string, unknown>,
  partial: boolean,
  lang: Lang,
  t: TFunction,
  nameOf: (f: FieldUi) => string,
): FormFieldError[] {
  const out: FormFieldError[] = [];
  for (const err of validateCodes(entity, body, partial)) {
    const f = fields.find((x) => x.name === err.field || x.ref?.idField === err.field);
    if (f) out.push({ name: nameOf(f), errors: [codeMessage(err, labelOf(f, lang), body[err.field], t)] });
  }
  for (const f of fields) {
    const member = f.kind === 'list' && f.of ? entities[f.of] : undefined;
    const list = rows[nameOf(f)];
    if (!member || !Array.isArray(list)) continue;
    const cols = formFields(member, 'create');
    list.forEach((row: Record<string, unknown> | undefined, i) => {
      if (!row || !Object.values(row).some((y) => y != null && y !== '')) return;
      for (const err of validateCodes(member, row, false)) {
        const c = cols.find((x) => x.name === err.field || x.ref?.idField === err.field);
        if (c) out.push({ name: [nameOf(f), i, nameOf(c)], errors: [codeMessage(err, labelOf(c, lang), row[err.field], t)] });
      }
    });
  }
  return out;
}
