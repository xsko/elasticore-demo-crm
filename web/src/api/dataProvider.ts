import type { CrudFilter, CrudSort, DataProvider } from '@refinedev/core';
import type { Page } from '@gen/apiSupport';
import { entityByResource, field } from '@/meta/registry';
import type { EntityUi } from '@/meta/types';
import { api } from './http';

/**
 * Refine's data provider over the API ElastiCORE generates for every exposed entity:
 * POST /search with pageNumber (zero-based), pageSize and sortCode ("amount-,name+"),
 * GET/DELETE /{id}, POST / to create, PUT / with the key in the body to update, and
 * POST /{id}/{command} for a lifecycle step. Where each entity lives and what its key is
 * comes from the screen model, so the provider knows no entity by name.
 */
export const dataProvider: DataProvider = {
  getApiUrl: () => '/api/crm',

  async getList({ resource, pagination, sorters, filters }) {
    const e = model(resource);
    const page = pagination?.mode === 'off' ? undefined : pagination;
    const body = {
      ...searchOf(e, filters),
      ...(page ? { pageNumber: (page.currentPage ?? 1) - 1, pageSize: page.pageSize ?? e.pageSize ?? 20 } : { pageSize: 1000 }),
      ...(sorters && sorters.length ? { sortCode: sortCodeOf(sorters) } : {}),
    };
    const result = await api<Page<Record<string, unknown>>>('POST', `${e.path}/search`, body);
    return { data: result.content.map((r) => withId(e, r)) as never[], total: result.totalElements };
  },

  async getOne({ resource, id }) {
    const e = model(resource);
    return { data: withId(e, await api<Record<string, unknown>>('GET', `${e.path}/${encodeURIComponent(String(id))}`)) as never };
  },

  async getMany({ resource, ids }) {
    const e = model(resource);
    const rows = await Promise.all(ids.map((id) => api<Record<string, unknown>>('GET', `${e.path}/${encodeURIComponent(String(id))}`)));
    return { data: rows.filter(Boolean).map((r) => withId(e, r)) as never[] };
  },

  async create({ resource, variables }) {
    const e = model(resource);
    return { data: withId(e, await api<Record<string, unknown>>('POST', e.path!, variables)) as never };
  },

  async update({ resource, id, variables }) {
    const e = model(resource);
    const body = { ...(variables as object), [e.key!]: id };
    return { data: withId(e, await api<Record<string, unknown>>('PUT', e.path!, body)) as never };
  },

  async deleteOne({ resource, id }) {
    const e = model(resource);
    await api<boolean>('DELETE', `${e.path}/${encodeURIComponent(String(id))}`);
    return { data: { id } as never };
  },

  async deleteMany({ resource, ids }) {
    const e = model(resource);
    await api<boolean[]>('POST', `${e.path}/delete`, ids.map((id) => ({ [e.key!]: id })));
    return { data: ids.map((id) => ({ id })) as never[] };
  },

  /** A lifecycle command: custom({ url: 'opportunity/12/win', method: 'post' }). */
  async custom({ url, method, payload }) {
    const path = url.startsWith('/') ? url : `/api/crm/${url}`;
    return { data: (await api<unknown>(method.toUpperCase(), path, payload)) as never };
  },
};

function model(resource: string): EntityUi {
  const e = entityByResource(resource);
  if (!e || !e.path || !e.key) throw new Error(`Unknown resource ${resource}`);
  return e;
}

/** Refine reads a row's id from "id": copy the entity's own key there. */
function withId(e: EntityUi, row: Record<string, unknown>): Record<string, unknown> {
  return row && e.key ? { ...row, id: row[e.key] } : row;
}

function sortCodeOf(sorters: CrudSort[]): string {
  return sorters.map((s) => s.field + (s.order === 'desc' ? '-' : '+')).join(',');
}

/**
 * Filters into the generated search DTO: a between field is fieldFrom/fieldTo, a
 * reference is its identity field, the rest the field itself (like-search fields match a
 * part of the value on the server).
 */
export function searchOf(e: EntityUi, filters: CrudFilter[] | undefined): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const f of filters ?? []) {
    if (!('field' in f)) continue;
    const v = f.value;
    if (v == null || v === '' || (Array.isArray(v) && v.length === 0)) continue;
    const def = field(e, f.field);
    const name = def?.kind === 'ref' && def.ref?.idField ? def.ref.idField : f.field;
    if (f.operator === 'between' && Array.isArray(v)) {
      if (v[0] != null && v[0] !== '') out[name + 'From'] = v[0];
      if (v[1] != null && v[1] !== '') out[name + 'To'] = v[1];
    } else if (f.operator === 'gte') {
      out[name + 'From'] = v;
    } else if (f.operator === 'lte') {
      out[name + 'To'] = v;
    } else {
      out[name] = Array.isArray(v) ? v[0] : v;
    }
  }
  return out;
}
