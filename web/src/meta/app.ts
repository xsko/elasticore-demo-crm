/**
 * What the server says about this installation (GET /api/crm/app), read once before the
 * screens render: the currency amounts are kept in, and the sample data's language.
 */
export interface AppInfo {
  currency: string;
  dataLanguage: string;
}

export const appInfo: AppInfo = { currency: 'USD', dataLanguage: 'en' };

export async function loadAppInfo(): Promise<void> {
  try {
    const res = await fetch('/api/crm/app', { credentials: 'same-origin' });
    if (res.ok) Object.assign(appInfo, await res.json());
  } catch {
    /* the defaults stay */
  }
}
