/**
 * The shape of the screen model the CRM's ElastiCORE rules write for each entity
 * (src/main/ts/<Entity>Ui.ts). Everything a generic screen needs to know about a field
 * comes from there - nothing about an entity is written in the screens themselves.
 */
export type FieldKind =
  | 'string' | 'text' | 'integer' | 'decimal' | 'money' | 'quantity' | 'boolean'
  | 'date' | 'datetime' | 'timestamp' | 'time' | 'enum' | 'ref' | 'list' | 'embedded' | 'json';

export interface FieldUi {
  readonly name: string;
  readonly kind: FieldKind;
  readonly label: string;
  readonly labels: Readonly<Record<string, string>>;
  readonly enum?: string;
  readonly ref?: { readonly entity: string; readonly idField?: string; readonly nameField?: string };
  readonly of?: string;
  readonly required?: boolean;
  readonly maxLength?: number;
  readonly search?: string;
  readonly key?: boolean;
  readonly generated?: boolean;
  readonly readOnly?: boolean;
  readonly createOnly?: boolean;
  readonly derived?: boolean;
  readonly display?: boolean;
  readonly encrypted?: boolean;
  readonly masked?: boolean;
  readonly adminOnly?: boolean;
  readonly default?: string;
  readonly terms?: readonly string[];
  readonly ui?: Readonly<Record<string, string | number | boolean>>;
}

export interface MoveUi {
  readonly command: string;
  readonly from: readonly string[];
  readonly to: string;
}

export interface LifecycleUi {
  readonly field: string;
  readonly initial?: string;
  readonly moves: readonly MoveUi[];
  readonly deletableIn?: readonly string[];
}

export interface EntityUi {
  readonly name: string;
  readonly root: boolean;
  readonly label: string;
  readonly labels: Readonly<Record<string, string>>;
  readonly path?: string;
  readonly key?: string;
  readonly defaultSort?: string;
  readonly pageSize?: number;
  readonly editable?: boolean;
  readonly terms?: readonly string[];
  readonly access?: Readonly<Record<string, string>>;
  readonly owner?: string;
  readonly ui?: Readonly<Record<string, string | number | boolean>>;
  readonly fields: readonly FieldUi[];
  readonly lifecycles: readonly LifecycleUi[];
}

export type Lang = 'ko' | 'en';
