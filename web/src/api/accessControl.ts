import type { AccessControlProvider } from '@refinedev/core';
import { entities, entityByResource, enumName } from '@/meta/registry';
import type { EntityUi } from '@/meta/types';
import { me, type Me } from './authProvider';

/**
 * What the menu and the buttons offer, read from the DSL: the entity's @access - the
 * same rules the server enforces - and its screen marker (@ui(admin): the administrator's
 * screen). The server is the one that refuses; the screen only hides what would be refused.
 *
 * An entry is the levels (public, user, owner, admin, none) and the role names of the
 * signed-in member's role field (REP, CUSTOMER...), joined by | (either) and & (both),
 * & binding first: read=owner|REP, update=REP&owner. The administrator passes every entry
 * but none; an operation the line does not name is the administrator's. Without a row the
 * screen reads owner as "signed in": the server narrows a search to the member's own rows.
 */
export function allowed(e: EntityUi, operation: string, who: Me | null): boolean {
  if (e.ui?.admin && who?.role !== 'A') return false;
  const access = e.access;
  if (!access || Object.keys(access).length === 0) return true;
  const entry = (access[operation] ?? access['*'] ?? 'admin').trim();
  if (who?.role === 'A') return entry !== 'none';
  const role = roleName(who);
  return entry.split('|').some((either) =>
    either.split('&').every((term) => passes(term.trim(), who, role)));
}

/**
 * Whether the member's search of this entity holds only their own rows: every way the read
 * entry lets them in names owner (a REP under read=REP&owner; not under read=owner|REP).
 * Its owner column would then say their own name on every row.
 */
export function seesOnlyOwn(e: EntityUi, who: Me | null): boolean {
  if (!who || who.role === 'A' || !e.owner || !e.access) return false;
  const entry = (e.access.read ?? e.access['*'] ?? 'admin').trim();
  const role = roleName(who);
  const open = entry.split('|').map((x) => x.split('&').map((t) => t.trim()))
    .filter((terms) => terms.every((term) => passes(term, who, role)));
  return open.length > 0 && open.every((terms) => terms.includes('owner'));
}

function passes(term: string, who: Me | null, role: string | undefined): boolean {
  switch (term) {
    case 'public':
      return true;
    case 'user':
    case 'owner':
      return who != null;
    case 'admin':
    case 'none':
      return false;
    default:
      return role != null && role === term;     // a role name: REP, CUSTOMER
  }
}

/** The constant name of the member's role (R -> REP), from the enum of the sign-in entity's role field. */
function roleName(who: Me | null): string | undefined {
  if (!who) return undefined;
  for (const e of Object.values(entities)) {
    const f = e.fields.find((x) => x.name === 'role' && x.kind === 'enum');
    if (f?.enum) return enumName(f.enum, who.role);
  }
  return undefined;
}

const ACTION: Record<string, string> = { list: 'read', show: 'read', create: 'create', edit: 'update', delete: 'delete' };

export const accessControlProvider: AccessControlProvider = {
  async can({ resource, action }) {
    if (!resource) return { can: true };
    const e = entityByResource(resource);
    if (!e) return { can: true };
    const who = await me();
    return { can: allowed(e, ACTION[action] ?? action, who) };
  },
  options: { buttons: { enableAccessControl: true, hideIfUnauthorized: true } },
};
