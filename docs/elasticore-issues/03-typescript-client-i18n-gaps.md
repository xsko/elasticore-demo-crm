# The TypeScript client carries field labels per language, but not the rest a screen needs

- **Version:** ElastiCORE 3, `typescriptClient = true`, `architecture = 'hexagonal'`
- **Kind:** feature request (three small gaps around `@label(en=...)`)

ElastiCORE 3 puts each field's translations in `XxxFields.f.labels` - very useful. A screen that
switches language still has to take three things from somewhere else:

1. **The entity's own label.** `meta: entity ... @label(en=Account) -- 고객사` reaches
   `crm-labels*.properties` (`Account=Account`) but not the TypeScript client. A menu or a page
   title has no generated source for "Account" / "고객사".
2. **Enum constant labels per language.** `XxxLabel` holds the first label column only
   (`{ PRS: "발굴" }`). An enum constant takes no `@label`, so the model can only carry other
   languages as extra columns (`labelEn`), which the client ignores.
3. **Validator messages.** `validateXxx` writes `"상품명 is required"` - an English sentence around
   the label in the model's language. A screen in either language shows a mixed sentence.

## Smallest model

```yaml
enumeration:
  OpportunityStage:
    meta: enum @db(code) @json(code) @label(en=Stage) -- 단계
    fields: { code: string(3) @code, label: string(20), labelEn: string(30) }
    enum:
      PROSPECT: PRS, 발굴, Prospecting
entity:
  Product:
    meta: entity @root @editable @expose(20) @label(en=Product) -- 상품
    fields:
      productId: long @id @sequence
      name: string(100)! @label(en=Name) -- 상품명
```

## Expected (any shape is fine)

- `export const ProductMeta = { label: "상품", labels: { en: "Product" } }` (or the entity in `XxxFields`)
- `OpportunityStageLabels = { PRS: { "": "발굴", en: "Prospecting" } }` - for instance from columns
  named `label` + `label<Lang>`, or a `@label(en=...)` allowed on a constant line
- validators that return a code and values (`{ code: "required", field: "name" }`), or messages in
  a language the caller chooses, so the screen words them

## Actual

`ProductFields` has no entity label; `OpportunityStageLabel = { PRS: "발굴" }`; `validateProduct`
returns `{ name: "상품명 is required" }`.

## What the demo does meanwhile

A team rule (`crm-ui-rules`) writes `<Entity>Ui.ts` with the entity label and its translations, and
appends `XxxLabels` to each enum file from the `label<Lang>` columns. The form validates from the
screen model with its own messages instead of `validateXxx`.
