import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import 'pretendard/dist/web/variable/pretendardvariable-dynamic-subset.css';
import '@/i18n';
import { configureGeneratedClient } from '@/api/http';
import { loadAppInfo } from '@/meta/app';
import App from './App';
import './styles.css';

configureGeneratedClient();

loadAppInfo().finally(() =>
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <App />
    </StrictMode>,
  ));
