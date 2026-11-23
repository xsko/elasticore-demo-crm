import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router';
import { Alert, Button, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { Bot, Plus, SendHorizontal } from 'lucide-react';
import Markdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { api } from '@/api/http';
import { currentLang } from '@/i18n';
import { signedInNow } from '@/api/authProvider';

interface Source { entity: string; id?: number | string; title: string; path?: string }
interface Turn { role: 'user' | 'assistant'; text: string; sources?: Source[] }
interface Answer { answer: string; sources: Source[]; conversationId: string }

/**
 * The AI assistant: a customer asks about their requests and the help articles, a sales rep
 * about their book. The server answers from the DSL's semantic model and tools made from
 * it, as the signed-in member - it sees what the member may see, nothing more.
 */
export function AssistantPage() {
  const { t } = useTranslation();
  const [turns, setTurns] = useState<Turn[]>([]);
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  const [conversation, setConversation] = useState<string | undefined>();
  const [error, setError] = useState<string | null>(null);
  const end = useRef<HTMLDivElement>(null);
  // A block body: scrollIntoView answers a Promise in recent browsers, and an effect must not return one.
  useEffect(() => { end.current?.scrollIntoView({ behavior: 'smooth' }); }, [turns, busy]);

  const staff = signedInNow()?.role !== 'C';
  const suggestions = t(staff ? 'assistant.suggestStaff' : 'assistant.suggestCustomer', { returnObjects: true }) as unknown as string[];

  const ask = async (question?: string) => {
    const q = (question ?? text).trim();
    if (!q || busy) return;
    setText('');
    setError(null);
    setTurns((x) => [...x, { role: 'user', text: q }]);
    setBusy(true);
    try {
      const a = await api<Answer>('POST', '/api/crm/assistant', { message: q, conversationId: conversation, language: currentLang() });
      setConversation(a.conversationId);
      setTurns((x) => [...x, { role: 'assistant', text: a.answer, sources: a.sources }]);
    } catch (e) {
      const status = (e as { statusCode?: number }).statusCode;
      setError(status === 503 ? t('assistant.unavailable') : (e as Error).message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="crm-page crm-assistant">
      <div className="crm-page-head">
        <div className="crm-page-title"><span className="crm-page-icon"><Bot size={18} /></span><h1>{t('assistant.title')}</h1></div>
        <Button icon={<Plus size={14} />} onClick={() => { setTurns([]); setConversation(undefined); }}>{t('assistant.newChat')}</Button>
      </div>
      <div className="crm-panel crm-chat">
        <div className="crm-chat-log">
          {turns.length === 0 && (
            <div className="crm-chat-welcome">
              <span className="crm-chat-badge"><Bot size={22} strokeWidth={1.75} /></span>
              <h2>{t('assistant.placeholder')}</h2>
              <p>{t('assistant.intro')}</p>
              <div className="crm-chat-suggest">
                {Array.isArray(suggestions) && suggestions.map((s) => (
                  <button key={s} type="button" onClick={() => ask(s)} disabled={busy}>{s}</button>
                ))}
              </div>
            </div>
          )}
          {turns.map((m, i) => (
            <div key={i} className={`crm-msg ${m.role}`}>
              <div className="crm-msg-body">{m.role === 'assistant' ? <Markdown remarkPlugins={[remarkGfm]}>{m.text}</Markdown> : m.text}</div>
              {m.sources && m.sources.length > 0 && (
                <div className="crm-msg-sources">
                  <span>{t('assistant.sources')}</span>
                  {m.sources.map((s, j) => s.path
                    ? <Link key={j} to={s.path}>{s.title}</Link>
                    : <span key={j} className="src">{s.title}</span>)}
                </div>
              )}
            </div>
          ))}
          {busy && <div className="crm-msg assistant"><div className="crm-msg-body crm-thinking">{t('assistant.thinking')}</div></div>}
          <div ref={end} />
        </div>
        {error && <Alert type="warning" showIcon message={error} style={{ margin: '0 16px 12px' }} />}
        <div className="crm-chat-input">
          <Input.TextArea value={text} onChange={(e) => setText(e.target.value)} autoSize={{ minRows: 1, maxRows: 5 }}
                          placeholder={t('assistant.placeholder')}
                          onPressEnter={(e) => { if (!e.shiftKey) { e.preventDefault(); ask(); } }} />
          <Button type="primary" icon={<SendHorizontal size={15} />} loading={busy} onClick={() => ask()}>{t('assistant.send')}</Button>
        </div>
      </div>
    </div>
  );
}
