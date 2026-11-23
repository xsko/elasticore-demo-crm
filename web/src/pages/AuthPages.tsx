import { useState } from 'react';
import { Link } from 'react-router';
import { Alert, Button, Form, Input, Segmented } from 'antd';
import { useLogin } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { Bot, Columns3, LifeBuoy } from 'lucide-react';
import { setLang } from '@/i18n';
import type { Lang } from '@/meta/types';
import { api } from '@/api/http';
import { entityOf, validateCodes } from '@/meta/registry';
import { codeMessage } from '@/form/codes';

function AuthFrame({ children }: { children: React.ReactNode }) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const points = [
    { icon: <Columns3 size={18} strokeWidth={1.75} />, title: t('app.f1'), body: t('app.f1Body') },
    { icon: <LifeBuoy size={18} strokeWidth={1.75} />, title: t('app.f2'), body: t('app.f2Body') },
    { icon: <Bot size={18} strokeWidth={1.75} />, title: t('app.f3'), body: t('app.f3Body') },
  ];
  return (
    <div className="crm-auth">
      <div className="crm-auth-side">
        <div className="crm-brand big">
          <span className="crm-brand-mark" aria-hidden>
            <svg viewBox="0 0 20 20" width="16" height="16"><path d="M4 4h12v3H7.5v1.6H14v2.8H7.5V13H16v3H4z" fill="currentColor" /></svg>
          </span>
          <span className="crm-brand-name">ElastiCORE <b>CRM</b></span>
        </div>
        <div className="crm-auth-pitch">
          <p className="crm-auth-tagline">{t('app.tagline')}</p>
          <ul className="crm-auth-points">
            {points.map((p) => (
              <li key={p.title}><span className="ic">{p.icon}</span><div><b>{p.title}</b><span>{p.body}</span></div></li>
            ))}
          </ul>
        </div>
        <div className="crm-auth-foot">{t('app.point2')} · {t('app.fictional')}</div>
      </div>
      <div className="crm-auth-main">
        <div className="crm-auth-lang">
          <Segmented size="small" value={lang} onChange={(v) => setLang(v as Lang)} options={[{ value: 'ko', label: '한국어' }, { value: 'en', label: 'English' }]} />
        </div>
        <div className="crm-auth-box">{children}</div>
      </div>
    </div>
  );
}

export function LoginPage() {
  const { t } = useTranslation();
  const { mutate: login, isPending } = useLogin<{ login: string; password: string }>();
  const [error, setError] = useState(false);
  const [form] = Form.useForm();
  const submit = (v: { login: string; password: string }) => {
    setError(false);
    login(v, { onSuccess: (r) => { if (!r.success) setError(true); } });
  };
  const demo = (login: string) => form.setFieldsValue({ login, password: 'demo1234' });
  return (
    <AuthFrame>
      <h2>{t('auth.signIn')}</h2>
      {error && <Alert type="error" showIcon message={t('auth.wrong')} style={{ marginBottom: 16 }} />}
      <Form form={form} layout="vertical" onFinish={submit} requiredMark={false}>
        <Form.Item name="login" label={t('auth.login')} rules={[{ required: true }]}><Input autoComplete="username" autoFocus /></Form.Item>
        <Form.Item name="password" label={t('auth.password')} rules={[{ required: true }]}><Input.Password autoComplete="current-password" /></Form.Item>
        <Button type="primary" htmlType="submit" block loading={isPending}>{t('auth.signIn')}</Button>
      </Form>
      <div className="crm-auth-alt">{t('auth.noAccount')} <Link to="/signup">{t('auth.signUp')}</Link></div>
      <div className="crm-demo">
        <div className="crm-demo-head">{t('auth.demo')} <span>{t('auth.demoHint')}</span></div>
        <div className="crm-demo-list">
          <button onClick={() => demo('admin')}><b>admin</b>{t('auth.admin')}</button>
          <button onClick={() => demo('minjun')}><b>minjun</b>{t('auth.rep')}</button>
          <button onClick={() => demo('seoyeon')}><b>seoyeon</b>{t('auth.rep')}</button>
          <button onClick={() => demo('customer')}><b>customer</b>{t('auth.customer')}</button>
        </div>
      </div>
    </AuthFrame>
  );
}

export function SignUpPage() {
  const { t } = useTranslation();
  const { mutate: login } = useLogin<{ login: string; password: string }>();
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();
  const labels: Record<string, string> = {
    login: t('auth.login'), password: t('auth.password'), name: t('auth.name'), email: t('auth.email'), company: t('auth.company'),
  };
  const submit = async (v: Record<string, string>) => {
    setError(null);
    // The sign-in entity's own constraints (login length and pattern, the password rule, the
    // email), as the DSL declares them - ElastiCORE's validateMemberCodes, not a copy here.
    const refused = validateCodes(entityOf('Member'), v, false).filter((e) => e.field in labels);
    if (refused.length) {
      form.setFields(refused.map((e) => ({ name: e.field, errors: [codeMessage(e, labels[e.field], v[e.field], t)] })));
      return;
    }
    setSaving(true);
    try {
      // The generated create of the sign-in entity is the sign-up (create=public in the DSL);
      // the role is an administrator's field, so a new account is always a customer.
      await api('POST', '/api/crm/member', v);
      login({ login: v.login, password: v.password });
    } catch (e) {
      const err = e as { message?: string; errors?: Record<string, string[]> };
      if (err.errors) form.setFields(Object.entries(err.errors).map(([name, errors]) => ({ name, errors })));
      setError(err.message ?? t('common.error'));
    } finally {
      setSaving(false);
    }
  };
  return (
    <AuthFrame>
      <h2>{t('auth.signUp')}</h2>
      <p className="crm-auth-note">{t('auth.signUpNote')}</p>
      {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}
      <Form form={form} layout="vertical" onFinish={submit} requiredMark={false}>
        <Form.Item name="login" label={t('auth.login')} rules={[{ required: true }]}><Input autoComplete="username" /></Form.Item>
        <Form.Item name="password" label={t('auth.password')} extra={t('auth.passwordRule')} rules={[{ required: true }]}>
          <Input.Password autoComplete="new-password" />
        </Form.Item>
        <Form.Item name="name" label={t('auth.name')} rules={[{ required: true, whitespace: true }]}><Input autoComplete="name" /></Form.Item>
        <Form.Item name="email" label={t('auth.email')} rules={[{ required: true }]}><Input autoComplete="email" /></Form.Item>
        <Form.Item name="company" label={t('auth.company')}><Input autoComplete="organization" /></Form.Item>
        <Button type="primary" htmlType="submit" block loading={saving}>{t('auth.signUp')}</Button>
      </Form>
      <div className="crm-auth-alt">{t('auth.haveAccount')} <Link to="/login">{t('auth.signIn')}</Link></div>
    </AuthFrame>
  );
}
