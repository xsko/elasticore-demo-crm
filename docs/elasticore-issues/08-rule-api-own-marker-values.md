# Rule API: an own marker's `key=value` pairs and bare flags are hard to read

- **Version:** ElastiCORE 3, team rules (`elasticore-rule-dev-guide.md` chapter 4, "Create your own marker")
- **Kind:** bug / API gap

A team marker such as `@ui(menu=sales, order=30, staff)` on an entity line, or `@ui(title=true)`
on a field line:

| Call | Answer |
|---|---|
| `EntitySpec.metaValue("ui")` | `null` |
| `EntitySpec.metaText("ui")` | `menu=sales, order=30` - **`staff` is gone**, pairs re-ordered |
| `FieldSpec.dslValue("ui")` | `null` |
| `FieldSpec.raw().getAnnotation("ui").getProperties()` | `{title=true}` - only by reaching into `raw()` |

So a bare flag mixed with pairs disappears silently, and a field marker's pairs are only reachable
through the internal `Field`/`Annotation` classes the guide says may change between versions.

## Expected

- `metaProperties("ui")` / `FieldSpec.dslProperties("ui")` returning every pair, and the flags as
  `name -> ""` (or a separate `flags`)
- or a DSL warning when a value without `=` is dropped

## What the demo does

Writes `staff=true` instead of `staff`, and reads field markers through `f.raw().getAnnotation(...)`.
