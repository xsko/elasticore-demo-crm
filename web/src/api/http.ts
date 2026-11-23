import type { HttpError } from '@refinedev/core';
import { configureApi } from '@gen/apiSupport';
import { currentLang } from '@/i18n';

/** The XSRF token the server put in a cookie, echoed in a header on every write. */
export function xsrfToken(): string {
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : '';
}

export function commonHeaders(): Record<string, string> {
  return { 'Accept-Language': currentLang(), 'X-XSRF-TOKEN': xsrfToken() };
}

/** The generated typed clients (OpportunityApi.win ...) send the same headers. */
export function configureGeneratedClient(): void {
  configureApi({ init: () => ({ credentials: 'same-origin', headers: commonHeaders() }) });
}

/** What the server's problem answer (RFC 9457) becomes for Refine: message + field errors. */
export function toHttpError(status: number, body: unknown): HttpError {
  const p = (body ?? {}) as { detail?: string; title?: string; code?: string; fieldErrors?: { field: string; message?: string }[] };
  const errors: Record<string, string[]> = {};
  for (const fe of p.fieldErrors ?? []) {
    if (fe.field) (errors[fe.field] ??= []).push(String(fe.message ?? ''));
  }
  return {
    statusCode: status,
    message: p.detail ?? p.title ?? `HTTP ${status}`,
    errors: Object.keys(errors).length ? errors : undefined,
    code: p.code,
  } as HttpError;
}

export async function api<T>(method: string, path: string, body?: unknown): Promise<T> {
  // A first visit has no XSRF cookie yet: a read gets one before the first write.
  if (method !== 'GET' && !xsrfToken()) await fetch('/api/crm/member/me', { credentials: 'same-origin' });
  const res = await fetch(path, {
    method,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...commonHeaders() },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await res.text();
  const json = text ? safeJson(text) : undefined;
  if (!res.ok) throw toHttpError(res.status, json);
  return json as T;
}

function safeJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}
