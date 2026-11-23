import { Result } from 'antd';
import { Lock, SearchX } from 'lucide-react';
import i18n from '@/i18n';

/** Plain notices for "not allowed" and "not found" - an icon and a sentence, no illustration. */
export function NoAccess() {
  return <Result icon={<Lock size={36} strokeWidth={1.5} color="#8a93a0" />} title={i18n.t('common.noAccess')} />;
}

export function NotFound({ title, extra }: { title?: string; extra?: React.ReactNode }) {
  return <Result icon={<SearchX size={36} strokeWidth={1.5} color="#8a93a0" />} title={title ?? i18n.t('record.notFound')} extra={extra} />;
}
