# ElastiCORE issues found while building the demo

Each file is written in the shape of ElastiCORE's bug report template (version, the smallest DSL,
what came out, what was expected).

## Filed 2026-11-21 - all done in the released ElastiCORE 3

All nine are closed and in the release, and the demo now uses them instead of its own work-arounds (last column).

| # | Kind | Title | Issue | The demo now |
|---|---|---|---|---|
| 01 | bug | [A TypeScript run never asks a `DomainArtifactContributor`](01-ts-run-ignores-domain-artifact-contributor.md) | [#266](https://github.com/xsko/ElastiCORE/issues/266) | keeps one file per entity (picked up by glob) |
| 02 | bug | [`@pattern` whose regular expression has parentheses is cut silently](02-pattern-with-parentheses-is-mangled.md) | [#267](https://github.com/xsko/ElastiCORE/issues/267) | writes the password rule as a lookahead |
| 03 | feature | [The TypeScript client lacks entity labels, enum labels per language, localizable validator messages](03-typescript-client-i18n-gaps.md) | [#268](https://github.com/xsko/ElastiCORE/issues/268) | enums carry `label_en`; the screens read the generated `XxxLabel` / `XxxLabels` (its own enum rule is gone) and check the DSL's constraints with `validateXxxCodes` |
| 04 | feature | [`@access` cannot name a role other than the administrator](04-access-by-role.md) | [#269](https://github.com/xsko/ElastiCORE/issues/269) | `read=REP&owner`, `answer=REP`; the hand-written staff guard is gone |
| 05 | feature | [`@Mask` hides the value from its owner, and an update writes the mask back](05-mask-for-everyone-and-the-update-round-trip.md) | [#270](https://github.com/xsko/ElastiCORE/issues/270) | the form shows the masked value and sends it back unchanged |
| 06 | feature | [A generated Excel / CSV export of a search](06-export-endpoint.md) | [#271](https://github.com/xsko/ElastiCORE/issues/271) | `methods="...,export"`; its own export service is gone |
| 07 | feature | [Ossie export: code meanings in the model's language only](07-ossie-code-meanings-per-language.md) | [#272](https://github.com/xsko/ElastiCORE/issues/272) | the counsellor reads `code_labels` through `ai_context` |
| 08 | bug | [Rule API: an own marker's pairs and bare flags are hard to read](08-rule-api-own-marker-values.md) | [#273](https://github.com/xsko/ElastiCORE/issues/273) | `@ui(board, pipeline)` read by `metaFlags` / `dslFlags` |
| 09 | feature | [Generated messages name the field, not its label](09-default-messages-use-field-names.md) | [#274](https://github.com/xsko/ElastiCORE/issues/274) | `labelLanguage: ko`; the Korean refusals use `{2}`-`{5}` |

## Filed 2026-11-22 - done in the released ElastiCORE 3

Both are closed and in the release, and the demo uses them.

| # | Kind | Title | Issue | The demo now |
|---|---|---|---|---|
| 10 | feature | [Export: an enumeration written by its label in the request's language](10-export-enum-labels.md) | [#289](https://github.com/xsko/ElastiCORE/issues/289) | the "every matching row" Excel sends `labels: "*"` |
| 11 | bug | [Rule API: the blind index of `@encrypt @search(eq)` looks like a declared field](11-rule-api-blind-index-field.md) | [#290](https://github.com/xsko/ElastiCORE/issues/290) | `crm-ui-rules` skips fields whose `blindIndexOf()` is set |

## Filed 2026-11-22 - open, in progress in phases

| # | Kind | Title | Issue |
|---|---|---|---|
| 12 | feature | [`testCode` for the v3 Java bundles: tests of what the DSL declares](12-test-code-for-v3-java.md) | [#295](https://github.com/xsko/ElastiCORE/issues/295) |

Agreed spec (2026-11-22): `testCode: "contract"` writes contract tests over HTTP (`@SpringBootTest` + MockMvc), no
Mockito, in this order - lifecycles and constraints, then `@access`, then `@Mask`/`@encrypt`, then `export`. Every test
goes through one support class per domain that the project owns (`signIn(...)`, `request(builder)`), checks status and
problem `code` (never the wording), creates its own rows, shares one application context, and makes skips visible.

**In use:** lifecycles, constraints and `@access`. The release writes 361 tests in 10 classes, and none is skipped.
- The demo's only change is in `CrmContractSupport.request(...)`: its XSRF cookie and header. As first generated,
  every write got 403 `csrf`.
- `signIn(role)` signs in a member of each role through the generated sign-in.
- One context serves the suite, and regenerating keeps the edits.

**What the demo reported** ([on the issue](https://github.com/xsko/ElastiCORE/issues/295#issuecomment-7082021355)),
all of it now in the release:
- a `@pattern` with a lookahead (the password rule) built no value, so Member had no test and no principal could be
  signed in;
- `@notblank` and `@email` were not tested;
- the support class's Javadoc gave the wrong reason for running as the application.

**Still to come:** `@Mask`/`@encrypt`, `export`, and an aggregate member's own constraints. Until then `CrmApiTest`
checks masking and the export by hand, and `QuoteContractTest` holds a hand-written test of `lines[0].quantity`.

Related issues filed earlier: #174 (enum codes in the Ossie extension), #178 (access that depends on the row).
