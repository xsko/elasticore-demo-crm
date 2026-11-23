import { Link } from 'react-router';
import { Lock } from 'lucide-react';
import i18n from '@/i18n';
import { entities, enumTone, resourceOf } from '@/meta/registry';
import { formatValue, isNumeric, refValue } from '@/meta/format';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';

/**
 * A value of an enumeration as a small pill. Its colour comes from the lifecycle that moves
 * it (registry.enumTone): new, the steps on the way, done, lost; any other list is neutral.
 */
export function StatusBadge({ enumName, value, lang }: { enumName?: string; value: unknown; lang: Lang }) {
  if (value == null || value === '') return null;
  return (
    <span className={`crm-pill tone-${enumTone(enumName, value)}`}>
      {formatValue({ name: '', kind: 'enum', enum: enumName, label: '', labels: {} }, value, lang)}
    </span>
  );
}

const AVATAR_COLORS = ['#0d6b78', '#3d5a99', '#8a5a1f', '#6b4f99', '#2b7a4b', '#9a3f53', '#3f6f8f', '#5f6b2e'];

/** A person's initials on a colour of their own (the same name always gets the same one). */
export function Initials({ name, size = 22 }: { name?: string | null; size?: number }) {
  const text = (name ?? '').trim();
  if (!text) return null;
  const parts = text.split(/\s+/);
  const latin = /[A-Za-z]/.test(text[0]);
  const letters = latin && parts.length > 1
    ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    : text.slice(0, latin ? 2 : 1).toUpperCase();
  let h = 0;
  for (const c of text) h = (h * 31 + c.charCodeAt(0)) >>> 0;
  return (
    <span className="crm-initials" style={{ width: size, height: size, fontSize: Math.round(size * 0.42), background: AVATAR_COLORS[h % AVATAR_COLORS.length] }}>
      {letters}
    </span>
  );
}

/** Whether a reference names a person who signs in (the target has a role field): shown with initials. */
function isPerson(target?: EntityUi): boolean {
  return !!target?.fields.some((f) => f.name === 'role' && f.kind === 'enum');
}

/** One cell of a list or a detail panel, by the field's kind. */
export function Cell({ f, row, lang, titleLink }: { f: FieldUi; row: Record<string, unknown>; lang: Lang; titleLink?: string }) {
  const value = refValue(f, row);
  if (titleLink) {
    return <Link className="crm-title-link" to={titleLink}>{formatValue(f, value, lang, row) || '-'}</Link>;
  }
  if (f.kind === 'enum') return <StatusBadge enumName={f.enum} value={value} lang={lang} />;
  if (f.kind === 'ref' && f.ref) {
    const target = entities[f.ref.entity];
    const id = f.ref.idField ? row[f.ref.idField] : undefined;
    const text = formatValue(f, value, lang, row);
    if (isPerson(target) && text) {
      return <span className="crm-person"><Initials name={text} />{text}</span>;
    }
    if (target?.root && target.path && id != null && !target.ui?.admin) {
      return <Link to={`/${resourceOf(target)}/show/${id}`}>{text}</Link>;
    }
    return <>{text}</>;
  }
  if (f.kind === 'boolean') {
    return <span className={value ? 'crm-flag on' : 'crm-flag'}>{formatValue(f, value, lang)}</span>;
  }
  const text = formatValue(f, value, lang, row);
  if (f.masked && text) {
    return (
      <span className="crm-masked" title={i18n.t('common.masked')}>
        {text} <Lock size={11} strokeWidth={2} />
      </span>
    );
  }
  return <span className={isNumeric(f) ? 'crm-num' : undefined}>{text}</span>;
}
