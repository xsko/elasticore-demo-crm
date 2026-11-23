# A TypeScript run (`elcore3ts`) never asks a `DomainArtifactContributor`

- **Version:** ElastiCORE 3 (Gradle plugin `io.elasticore`, Spring Boot 4.0.8, Java 17, `architecture = 'hexagonal'`)
- **Generation:** `typescriptClient = true`, team rules listed for TypeScript in `env.yml`
- **Kind:** bug (or a documentation gap)

## What happens

A rule set that implements `DomainArtifactContributor` and is listed for TypeScript is never asked
for its files in a TypeScript run. No file is written, nothing is logged, and no warning is given.
The same rule set's `ArtifactContributor` (one file per entity) and its `target = "enum"` rule work
in the same run.

`elasticore-ddd-customize-guide.md` (around line 1079) tells a team to "make a separate one for
TypeScript: a rule that uses TypeScript and an `elasticore-template/ts/` skeleton, and list it with
`ts-model` under `ts:` of `publisher.rules`". That works for per-entity files only.

## Smallest model

`env.yml`
```yaml
elasticore:
  enable: true
  config:
    domainName: crm
    framework: springboot
    mode: jpa
    j2ee: jakarta
    apiPath: "/api"
    publisher:
      rules:
        ts: [ts-model, com.acme.rules.UiRules]
  namespace:
    base: com.acme.crm
```

`model/entity.yml`
```yaml
entity:
  Product:
    meta: entity @root @editable @expose(20)
    fields:
      productId: long @id @sequence
      name: string(100)!
```

`UiRules.java` (in a rules module, `elasticore project(':rules')`)
```java
public class UiRules implements CodeRules, DomainArtifactContributor {
    @Override public List<ContributedArtifact> artifactsFor(DomainSpec d) {
        return List.of(ContributedArtifact.of("uiModel", "dto", d.getName() + "UiModel"));
    }
    @Rule(value = "ui-model", target = "uiModel")
    void model(DomainSpec d) { ctx(d).var("domain", d.getName()); }
}
```
with `src/main/resources/elasticore-template/ts/uiModel.tmpl` = `export const domain = "${domain}";`

## Expected

`src/main/ts/crmUiModel.ts` is written, as `apiSupport.ts` is. Or, if a domain file is not meant
for TypeScript, a DSL warning that says so and points to `ArtifactContributor`.

## Actual

```
=== elcore3: 12 written / 48 unchanged / 1 skipped / 0 failed ===
```
No `crmUiModel.ts`, no line for it. `RulesGenRunner` returns from the TypeScript branch after
`generateEnums` and `apiSupport` (around line 1529), so `emitContributedDomain` is never reached.

## What the demo does meanwhile

One file per entity (`<Entity>Ui.ts`, an `ArtifactContributor`); the screens collect them with
Vite's `import.meta.glob`.
