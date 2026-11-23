import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Button, Segmented, Space } from 'antd';
import { NoAccess, NotFound } from '@/layout/Notices';
import { useInvalidate } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import { Kanban, List, Plus } from 'lucide-react';
import { entityByResource, labelOf, resourceOf } from '@/meta/registry';
import type { Lang } from '@/meta/types';
import { allowed } from '@/api/accessControl';
import { signedInNow } from '@/api/authProvider';
import { CrmGrid } from '@/grid/CrmGrid';
import { EntityForm } from '@/form/EntityForm';
import { EntityIcon } from '@/layout/icons';

/** The list page of any entity: its name, the "new" action and the grid. */
export function ListPage() {
  const { resource = '' } = useParams();
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const navigate = useNavigate();
  const invalidate = useInvalidate();
  const [creating, setCreating] = useState(false);
  const entity = entityByResource(resource);
  if (!entity) return <NotFound />;
  const who = signedInNow();
  if (!allowed(entity, 'read', who)) return <NoAccess />;

  return (
    <div className="crm-page">
      <div className="crm-page-head">
        <div className="crm-page-title">
          <span className="crm-page-icon"><EntityIcon name={String(entity.ui?.icon ?? '')} size={18} /></span>
          <h1>{labelOf(entity, lang)}</h1>
        </div>
        <Space size={8}>
          {entity.ui?.board && <ViewSwitch resource={resourceOf(entity)} current="list" />}
          {allowed(entity, 'create', who) && (
            <Button type="primary" icon={<Plus size={14} />} onClick={() => setCreating(true)}>{t('common.new')}</Button>
          )}
        </Space>
      </div>
      <div className="crm-panel">
        <CrmGrid key={entity.name} entity={entity} />
      </div>
      <EntityForm entity={entity} open={creating} onClose={() => setCreating(false)}
                  onSaved={(row) => { invalidate({ resource: resourceOf(entity), invalidates: ['list'] }); navigate(`/${resourceOf(entity)}/show/${row[entity.key!]}`); }} />
    </div>
  );
}

/** List or board, for an entity the DSL marks @ui(board=true). */
export function ViewSwitch({ resource, current }: { resource: string; current: 'list' | 'board' }) {
  const navigate = useNavigate();
  const { t } = useTranslation();
  return (
    <Segmented size="middle" value={current}
      onChange={(v) => navigate(v === 'board' ? `/board/${resource}` : `/${resource}`)}
      options={[{ value: 'list', icon: <List size={14} />, label: t('common.list') }, { value: 'board', icon: <Kanban size={14} />, label: t('common.board') }]} />
  );
}