import { useEffect, useMemo, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import { Dropdown, Menu, Segmented, Tooltip } from 'antd';
import { useGetIdentity, useLogout } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { Bot, ChevronsLeft, ChevronsRight, Columns3, LayoutDashboard, LogOut } from 'lucide-react';
import { labelOf, resourceOf, roots } from '@/meta/registry';
import type { Lang } from '@/meta/types';
import { allowed } from '@/api/accessControl';
import type { Me } from '@/api/authProvider';
import { setLang } from '@/i18n';
import { Initials } from '@/grid/Cells';
import { EntityIcon } from './icons';
import { ErrorBoundary } from './ErrorBoundary';

const GROUPS = ['sales', 'service', 'catalog', 'admin'];

/**
 * The frame: a dark rail with the product, the menu grouped by the DSL's @ui(menu=...) -
 * each entity the member may read, in @ui(order=...) order - and, at its foot, the language
 * and the signed-in member. Nothing here names an entity.
 */
export function AppShell() {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const { data: who } = useGetIdentity<Me>();
  const { mutate: logout } = useLogout();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem('crm.nav') === 'collapsed');
  useEffect(() => { localStorage.setItem('crm.nav', collapsed ? 'collapsed' : 'open'); }, [collapsed]);

  const staff = who != null && who.role !== 'C';
  const icon = (node: React.ReactNode) => <span className="crm-nav-icon">{node}</span>;
  const items = useMemo(() => {
    const top = staff
      ? [
          { key: '/', icon: icon(<LayoutDashboard size={16} strokeWidth={1.75} />), label: t('menu.home') },
          { key: '/pipeline', icon: icon(<Columns3 size={16} strokeWidth={1.75} />), label: t('menu.pipeline') },
        ]
      : [];
    const grouped = GROUPS.map((g) => {
      const children = roots
        .filter((e) => (e.ui?.menu ?? 'sales') === g && allowed(e, 'read', who ?? null))
        .map((e) => ({ key: `/${resourceOf(e)}`, icon: icon(<EntityIcon name={String(e.ui?.icon ?? '')} />), label: labelOf(e, lang) }));
      return children.length ? { type: 'group' as const, key: g, label: collapsed ? null : t(`menu.${g}`), children } : null;
    }).filter(Boolean);
    const assistant = { type: 'group' as const, key: 'ai', label: collapsed ? null : t('menu.ai'),
      children: [{ key: '/assistant', icon: icon(<Bot size={16} strokeWidth={1.75} />), label: t('menu.assistant') }] };
    return [...top, ...grouped, assistant];
  }, [who, lang, collapsed, staff, t]);   // eslint-disable-line react-hooks/exhaustive-deps

  const selected = '/' + (location.pathname.split('/')[1] ?? '');
  const selectedKey = selected === '/board' ? '/' + (location.pathname.split('/')[2] ?? '') : selected;

  return (
    <div className={`crm-app${collapsed ? ' collapsed' : ''}`}>
      <aside className="crm-rail crm-sider">
        <Link to={staff ? '/' : '/ticket'} className="crm-brand">
          <span className="crm-brand-mark" aria-hidden>
            <svg viewBox="0 0 20 20" width="14" height="14"><path d="M4 4h12v3H7.5v1.6H14v2.8H7.5V13H16v3H4z" fill="currentColor" /></svg>
          </span>
          {!collapsed && <span className="crm-brand-name">ElastiCORE <b>CRM</b></span>}
        </Link>
        <div className="crm-rail-menu">
          <Menu theme="dark" mode="inline" inlineCollapsed={collapsed} selectedKeys={[selectedKey]} items={items as never}
                onClick={({ key }) => navigate(key)} />
        </div>
        <div className="crm-rail-foot">
          {!collapsed && (
            <Segmented size="small" block value={lang} onChange={(v) => setLang(v as Lang)} className="crm-lang"
                       options={[{ value: 'ko', label: '한국어' }, { value: 'en', label: 'English' }]} />
          )}
          {who && (
            <Dropdown trigger={['click']} placement="topLeft" menu={{ items: [
              { key: 'role', disabled: true, label: <span className="crm-user-role">{who.login} · {t(`auth.${roleKey(who.role)}`)}</span> },
              ...(collapsed ? [{ key: 'lang', label: lang === 'ko' ? 'English' : '한국어', onClick: () => setLang(lang === 'ko' ? 'en' : 'ko') }] : []),
              { type: 'divider' as const },
              { key: 'out', icon: <LogOut size={14} />, label: t('auth.signOut'), onClick: () => logout() },
            ] }}>
              <button className="crm-user">
                <Initials name={who.name} size={28} />
                {!collapsed && (
                  <span className="crm-user-text">
                    <span className="crm-user-name">{who.name}</span>
                    <span className="crm-user-sub">{t(`auth.${roleKey(who.role)}`)}</span>
                  </span>
                )}
              </button>
            </Dropdown>
          )}
          <Tooltip title={collapsed ? t('common.expand') : t('common.collapse')} placement="right">
            <button className="crm-collapse" onClick={() => setCollapsed(!collapsed)} aria-label="toggle navigation">
              {collapsed ? <ChevronsRight size={15} /> : <ChevronsLeft size={15} />}
            </button>
          </Tooltip>
        </div>
      </aside>
      <main className="crm-main">
        <ErrorBoundary resetKey={location.pathname}><Outlet /></ErrorBoundary>
      </main>
    </div>
  );
}

function roleKey(role: Me['role']): string {
  return role === 'A' ? 'admin' : role === 'R' ? 'rep' : 'customer';
}
