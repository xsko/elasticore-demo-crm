# Rule API: the blind index of `@encrypt @search(eq)` looks like a declared field

- **Version:** ElastiCORE 3
- **Kind:** bug (rule API)

```yaml
Contact:
  meta: entity @root @expose(20)
  fields:
    contactId: long @id @sequence
    email:     string(100) @email @encrypt @s(eq) -- 이메일
    phone:     string(20) @encrypt(digits) @s(eq) -- 전화번호
```

A team rule set (`ArtifactContributor`, `@Rule(target = "entity")`) that walks
`EntitySpec.fields()` gets `emailBidx` and `phoneBidx` beside the declared fields: a `string(22)`
with no label and no other sign of what it is. `EntitySpec.dtoFields()` gives the same list when
the DTO declares no fields of its own. The generated DTO, the TypeScript client and the Ossie
export leave them out, but nothing in `elasticore-rule-dev-guide.md` (chapter 10) says how a rule
set does the same. The demo's screen model showed an `emailBidx` input on the contact form until it
skipped fields marked `bidxof` / `bidxdigits` - the internal constants
`EncryptRules.INDEX_OF` / `INDEX_DIGITS`, which the guide does not name.

## Expected

One of:

- `FieldSpec.blindIndexOf()` - the name of the field it indexes, or null - documented in chapter 10;
- or `fields()` leaves the index out for rule sets (`allFields()` keeping it for the table's view).
