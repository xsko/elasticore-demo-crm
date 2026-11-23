import type { AuthProvider } from '@refinedev/core';
import { api } from './http';

export interface Me {
  memberId: number;
  login: string;
  name: string;
  email?: string;
  company?: string;
  role: 'A' | 'R' | 'C';
}

let cached: Me | null | undefined;

/** Who is signed in: the generated GET /member/me (401 for a visitor). */
export async function me(force = false): Promise<Me | null> {
  if (cached !== undefined && !force) return cached;
  try {
    cached = await api<Me>('GET', '/api/crm/member/me');
  } catch {
    cached = null;
  }
  return cached;
}

export function signedInNow(): Me | null {
  return cached ?? null;
}

export const isStaff = (m: Me | null) => m != null && m.role !== 'C';
export const isAdmin = (m: Me | null) => m != null && m.role === 'A';

/** The generated sign-in (POST /member/signin, a session cookie) behind Refine's auth hooks. */
export const authProvider: AuthProvider = {
  async login({ login, password }: { login: string; password: string }) {
    try {
      cached = await api<Me>('POST', '/api/crm/member/signin', { login, password });
      return { success: true, redirectTo: '/' };
    } catch (e) {
      cached = null;
      return { success: false, error: { name: 'signin', message: (e as Error).message } };
    }
  },
  async logout() {
    try {
      await api('POST', '/api/crm/member/signout');
    } finally {
      cached = null;
    }
    return { success: true, redirectTo: '/login' };
  },
  async check() {
    const m = await me();
    return m ? { authenticated: true } : { authenticated: false, redirectTo: '/login', logout: true };
  },
  async getIdentity() {
    return me();
  },
  async onError(error) {
    const status = (error as { statusCode?: number })?.statusCode;
    if (status === 401) {
      cached = null;
      return { logout: true, redirectTo: '/login' };
    }
    return { error };
  },
};
