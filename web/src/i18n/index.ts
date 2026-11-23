import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ko from './ko.json';
import en from './en.json';
import type { Lang } from '@/meta/types';

/**
 * The screens' own words (buttons, headings, messages). What a field or a value is
 * called comes from the DSL instead (labels per language in the screen model), so this
 * file never names an entity's field.
 */
const KEY = 'crm.lang';

function initial(): Lang {
  try {
    const saved = localStorage.getItem(KEY);
    if (saved === 'ko' || saved === 'en') return saved;
  } catch {
    /* storage may be unavailable */
  }
  return navigator.language?.toLowerCase().startsWith('ko') ? 'ko' : 'en';
}

i18n.use(initReactI18next).init({
  resources: { ko: { translation: ko }, en: { translation: en } },
  lng: initial(),
  fallbackLng: 'ko',
  interpolation: { escapeValue: false },
});

export function currentLang(): Lang {
  return (i18n.language === 'en' ? 'en' : 'ko') as Lang;
}

export function setLang(lang: Lang): void {
  i18n.changeLanguage(lang);
  try {
    localStorage.setItem(KEY, lang);
  } catch {
    /* ignore */
  }
  document.documentElement.lang = lang;
}

document.documentElement.lang = currentLang();

export default i18n;
