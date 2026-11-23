import { Refine, Authenticated } from '@refinedev/core';
import routerProvider, { CatchAllNavigate } from '@refinedev/react-router';
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router';
import { App as AntApp, ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import enUS from 'antd/locale/en_US';
import { useTranslation } from 'react-i18next';
import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import { dataProvider } from '@/api/dataProvider';
import { authProvider, signedInNow } from '@/api/authProvider';
import { accessControlProvider } from '@/api/accessControl';
import { labelOf, resourceOf, roots } from '@/meta/registry';
import type { Lang } from '@/meta/types';
import { theme } from '@/theme';
import { AppShell } from '@/layout/AppShell';
import { ListPage } from '@/pages/ListPage';
import { RecordPage } from '@/pages/RecordPage';
import { BoardPage } from '@/pages/BoardPage';
import { HomePage } from '@/pages/HomePage';
import { AssistantPage } from '@/pages/AssistantPage';
import { LoginPage, SignUpPage } from '@/pages/AuthPages';

/**
 * One resource per root entity of the DSL (from the generated screen models), the same
 * list and record pages for each, and the few screens of their own: home, board, assistant.
 */
export default function App() {
  const { i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  dayjs.locale(lang);
  const resources = roots.map((e) => ({
    name: resourceOf(e),
    list: `/${resourceOf(e)}`,
    show: `/${resourceOf(e)}/show/:id`,
    meta: { label: labelOf(e, lang) },
  }));
  const boardEntity = roots.find((e) => e.ui?.pipeline) ?? roots.find((e) => e.ui?.board);

  return (
    <BrowserRouter>
      <ConfigProvider theme={theme} locale={lang === 'ko' ? koKR : enUS}>
        <AntApp>
          <Refine
            dataProvider={dataProvider}
            authProvider={authProvider}
            accessControlProvider={accessControlProvider}
            routerProvider={routerProvider}
            resources={resources}
            options={{ syncWithLocation: true, disableTelemetry: true, warnWhenUnsavedChanges: false, reactQuery: { clientConfig: { defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } } } } }}
          >
            <Routes>
              <Route element={<Authenticated key="app" fallback={<CatchAllNavigate to="/login" />}><AppShell /></Authenticated>}>
                <Route index element={<StaffHome />} />
                {boardEntity && <Route path="/pipeline" element={<BoardPage resource={resourceOf(boardEntity)} />} />}
                <Route path="/board/:resource" element={<BoardPage />} />
                <Route path="/assistant" element={<AssistantPage />} />
                <Route path="/:resource" element={<ListPage />} />
                <Route path="/:resource/show/:id" element={<RecordPage />} />
              </Route>
              <Route element={<Authenticated key="auth" fallback={<Outlet />}><Navigate to="/" replace /></Authenticated>}>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignUpPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </Refine>
        </AntApp>
      </ConfigProvider>
    </BrowserRouter>
  );
}

/** A customer account's home is their requests; staff get the sales home. */
function StaffHome() {
  const who = signedInNow();
  const tickets = roots.find((e) => e.ui?.menu === 'service' && e.ui?.board);
  if (who?.role === 'C' && tickets) return <Navigate to={`/${resourceOf(tickets)}`} replace />;
  return <HomePage />;
}
