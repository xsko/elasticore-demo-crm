import { useEffect, useMemo, useState } from 'react';
import { App, Button, Col, DatePicker, Drawer, Form, Input, InputNumber, Row, Select, Space, Switch, Table } from 'antd';
import { useCreate, useUpdate, type HttpError } from '@refinedev/core';
import { useTranslation } from 'react-i18next';
import type { TFunction } from 'i18next';
import { Plus, Trash2 } from 'lucide-react';
import dayjs from 'dayjs';
import { entities, enumLabel, enumValues, formFields, labelOf, resourceOf } from '@/meta/registry';
import type { EntityUi, FieldUi, Lang } from '@/meta/types';
import { RefSelect } from './RefSelect';
import { formErrors } from './codes';

/**
 * The create and edit form of any entity, built from its screen model: an input per
 * field the request may write (the DSL's @readonly, lifecycle, derived and generated
 * fields are left out; on edit also what is given once at creation), the DSL's limits as
 * rules, and the server's field errors beside the field they are about. A list of
 * members (a quote's lines) is a small table of rows.
 */
export function EntityForm({ entity, open, record, onClose, onSaved, defaults }: {
  entity: EntityUi;
  open: boolean;
  record?: Record<string, unknown> | null;
  onClose: () => void;
  onSaved?: (row: Record<string, unknown>) => void;
  defaults?: Record<string, unknown>;
}) {
  const { t, i18n } = useTranslation();
  const lang = (i18n.language === 'en' ? 'en' : 'ko') as Lang;
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const mode = record ? 'edit' : 'create';
  const fields = useMemo(() => formFields(entity, mode), [entity, mode]);
  const { mutateAsync: create } = useCreate();
  const { mutateAsync: update } = useUpdate();
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue(toForm(fields, record ?? defaults ?? {}));
  }, [open, record]);   // eslint-disable-line react-hooks/exhaustive-deps

  const submit = async () => {
    const values = await form.validateFields();
    const body = fromForm(fields, values, record);
    // The DSL's constraints, as the server checks them (ElastiCORE's validateXxxCodes):
    // refused here, in the screen's words, before a request is sent.
    const refused = formErrors(entity, fields, body, values, mode === 'edit', lang, t, formName);
    if (refused.length) {
      form.setFields(refused);
      return;
    }
    setSaving(true);
    try {
      const resource = resourceOf(entity);
      const res = record
        ? await update({ resource, id: record.id as string, values: body, successNotification: false, errorNotification: false })
        : await create({ resource, values: body, successNotification: false, errorNotification: false });
      message.success(t('common.saved'));
      onSaved?.(res.data as Record<string, unknown>);
      onClose();
    } catch (e) {
      const err = e as HttpError;
      if (err.errors) {
        form.setFields(Object.entries(err.errors).map(([name, msgs]) => ({ name: nameFor(fields, name), errors: ([] as string[]).concat(msgs as string[]) })));
      }
      message.error(err.message || t('common.error'));
    } finally {
      setSaving(false);
    }
  };

  const title = `${labelOf(entity, lang)} ${record ? t('common.edit') : t('common.new')}`;
  return (
    <Drawer open={open} onClose={onClose} title={title} width={640} destroyOnHidden
            extra={<Space><Button onClick={onClose}>{t('common.cancel')}</Button><Button type="primary" loading={saving} onClick={submit}>{t('common.save')}</Button></Space>}>
      <Form form={form} layout="vertical" requiredMark className="crm-form">
        <Row gutter={16}>
          {fields.map((f) => (
            <Col key={f.name} span={wide(f) ? 24 : 12}>
              {f.kind === 'list' && f.of ? (
                <LinesInput field={f} lang={lang} />
              ) : (
                <Form.Item name={formName(f)} label={labelOf(f, lang)} rules={rules(f, lang, t)}
                           valuePropName={f.kind === 'boolean' ? 'checked' : 'value'}
                           extra={f.masked && mode === 'edit' && record?.[f.name] ? t('common.maskedKept') : undefined}>
                  {input(f, lang)}
                </Form.Item>
              )}
            </Col>
          ))}
        </Row>
      </Form>
    </Drawer>
  );
}


function wide(f: FieldUi): boolean {
  return f.kind === 'text' || f.kind === 'list' || (f.maxLength ?? 0) > 150 || f.ui?.widget === 'textarea';
}

function formName(f: FieldUi): string {
  return f.kind === 'ref' && f.ref?.idField ? f.ref.idField : f.name;
}

/** The form field a server field error is about: a field, or a member's field by its path (lines[0].quantity). */
function nameFor(fields: FieldUi[], serverField: string): string | (string | number)[] {
  const member = /^(\w+)\[(\d+)\]\.(\w+)$/.exec(serverField);
  if (member) return [member[1], Number(member[2]), member[3]];
  const f = fields.find((x) => x.name === serverField || x.ref?.idField === serverField);
  return f ? formName(f) : serverField;
}

function rules(f: FieldUi, lang: Lang, t: TFunction) {
  const label = labelOf(f, lang);
  const out: object[] = [];
  if (f.required && f.kind !== 'boolean') {
    // whitespace only on text: any second key besides required makes the validator check the
    // value as a string, and a reference's identity (a number) would then fail as "missing".
    const text = f.kind === 'string' || f.kind === 'text';
    out.push(text ? { required: true, whitespace: true, message: t('common.required', { label }) }
                  : { required: true, message: t('common.required', { label }) });
  }
  if (f.maxLength && (f.kind === 'string' || f.kind === 'text')) out.push({ max: f.maxLength, message: t('common.maxLength', { label, n: f.maxLength }) });
  // Every other constraint (@min, @max, @length, @pattern, @email, @minsize) is the generated
  // validator's, checked on save (formErrors).
  return out;
}

function input(f: FieldUi, lang: Lang) {
  switch (f.kind) {
    case 'text':
      return <Input.TextArea autoSize={{ minRows: 3, maxRows: 10 }} maxLength={f.maxLength} />;
    case 'enum':
      return <Select allowClear options={enumValues(f.enum!).map((v) => ({ value: v, label: enumLabel(f.enum, v, lang) }))} />;
    case 'ref':
      return <RefSelect entity={f.ref!.entity} />;
    case 'boolean':
      return <Switch size="small" />;
    case 'money':
    case 'decimal':
      return <InputNumber style={{ width: '100%' }} formatter={(v) => (v == null ? '' : `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ','))}
                          parser={(v) => (v ? Number(v.replace(/,/g, '')) : null) as never} />;
    case 'integer':
    case 'quantity':
      return <InputNumber style={{ width: '100%' }} precision={0} />;
    case 'date':
      return <DatePicker style={{ width: '100%' }} />;
    case 'datetime':
    case 'timestamp':
      return <DatePicker showTime={{ format: 'HH:mm' }} format="YYYY-MM-DD HH:mm" style={{ width: '100%' }} />;
    default:
      return <Input maxLength={f.maxLength} type={f.name === 'password' ? 'password' : 'text'} />;
  }
}

/** A list of an aggregate's members (a quote's lines) as rows of their own fields. */
function LinesInput({ field, lang }: { field: FieldUi; lang: Lang }) {
  const { t } = useTranslation();
  const member = entities[field.of!];
  const cols = member ? formFields(member, 'create') : [];
  return (
    <Form.Item label={labelOf(field, lang)} required={field.required}>
      <Form.List name={field.name} initialValue={[{}]}>
        {(rows, { add, remove }, { errors }) => (
          <>
            <Table size="small" pagination={false} rowKey="key" dataSource={rows}
              columns={[
                ...cols.map((c) => ({
                  title: labelOf(c, lang), key: c.name,
                  render: (_: unknown, row: { name: number }) => (
                    <Form.Item name={[row.name, formName(c)]} rules={rules(c, lang, t)} style={{ margin: 0 }}>
                      {input(c, lang)}
                    </Form.Item>
                  ),
                })),
                { title: '', key: 'x', width: 40, render: (_: unknown, row: { name: number }) => <Button size="small" type="text" icon={<Trash2 size={13} />} onClick={() => remove(row.name)} /> },
              ]} />
            <Button size="small" type="dashed" icon={<Plus size={13} />} style={{ marginTop: 8 }} onClick={() => add({})}>{t('common.new')}</Button>
            <Form.ErrorList errors={errors} />
          </>
        )}
      </Form.List>
    </Form.Item>
  );
}

function toForm(fields: FieldUi[], row: Record<string, unknown>): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const f of fields) {
    const v = f.kind === 'ref' && f.ref?.idField ? row[f.ref.idField] : row[f.name];
    if (v == null) continue;
    if (f.kind === 'date') out[formName(f)] = dayjs(String(v));
    else if (f.kind === 'datetime') out[formName(f)] = dayjs(String(v).replace(' ', 'T'));
    else if (f.kind === 'timestamp') out[formName(f)] = dayjs(String(v));
    else out[formName(f)] = v;
  }
  return out;
}

function fromForm(fields: FieldUi[], values: Record<string, unknown>, record?: Record<string, unknown> | null): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const f of fields) {
    const key = formName(f);
    let v = values[key];
    if (v == null) {
      if (record) out[key] = null;
      continue;
    }
    if (dayjs.isDayjs(v)) {
      v = f.kind === 'date' ? v.format('YYYY-MM-DD') : f.kind === 'timestamp' ? v.toISOString() : v.format('YYYY-MM-DD HH:mm:ss');
    }
    if (f.kind === 'list' && Array.isArray(v)) v = v.filter((x) => x && Object.values(x).some((y) => y != null && y !== ''));
    out[key] = v;
  }
  return out;
}
