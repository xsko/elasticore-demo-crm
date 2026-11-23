import { Component, type ReactNode } from 'react';
import { Button, Result } from 'antd';
import i18n from '@/i18n';

/** A screen that fails shows what went wrong in its place, and the rest of the frame keeps working. */
export class ErrorBoundary extends Component<{ children: ReactNode; resetKey?: string }, { error: Error | null }> {
  state = { error: null as Error | null };

  static getDerivedStateFromError(error: Error) {
    return { error };
  }

  componentDidUpdate(prev: { resetKey?: string }) {
    if (prev.resetKey !== this.props.resetKey && this.state.error) this.setState({ error: null });
  }

  componentDidCatch(error: Error) {
    console.error('screen failed', error);
  }

  render() {
    if (!this.state.error) return this.props.children;
    return (
      <Result status="warning" title={i18n.t('common.error')} subTitle={this.state.error.message}
              extra={<Button onClick={() => this.setState({ error: null })}>{i18n.t('common.refresh')}</Button>} />
    );
  }
}
