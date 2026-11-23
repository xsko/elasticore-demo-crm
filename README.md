# ElastiCORE CRM demo

**English** | [한국어](README.ko.md)

A CRM built from one **ElastiCORE 3** DSL: sales (leads, opportunities, accounts, contacts, activities,
quotes) and customer service (tickets, help articles), with React screens, English and Korean, Excel export, an
Apache Ossie semantic model and an AI assistant - all following the DSL.

> Change one line of the DSL and the database column, the API, the list, search and form screens, the Excel headers,
> the labels in both languages, the Ossie model and what the AI assistant knows change with it.
> `scripts/dsl-change-demo.ps1` shows exactly that.

## Run it

You need JDK 17+ and Node.js 20+ (for the screens). The database is an embedded H2 file (`./data/crm-en` or
`./data/crm-ko`), filled with sample data on the first start. ElastiCORE 3 comes from Maven Central; the version
the build uses is `elasticoreVersion` in `gradle.properties`.

```bash
./gradlew bootRun            # Windows: gradlew.bat bootRun
# http://localhost:8080
```

| Login | Role | Password |
|---|---|---|
| `admin` | Administrator - all data | `demo1234` |
| `minjun`, `seoyeon`, `jihoon`, `yuna` | Sales rep - their own accounts and deals; reads and answers every ticket | `demo1234` |
| `customer`, `haneul`, `garam` | Customer - their own tickets, help articles, the AI assistant | `demo1234` |

**Sample data language:** English with amounts in dollars by default. For Korean data with amounts in won, choose it
on the first start:

```bash
CRM_SEED_LANGUAGE=ko ./gradlew bootRun      # default en; CRM_CURRENCY=USD|KRW sets the currency on its own
# Windows PowerShell: $env:CRM_SEED_LANGUAGE='ko'; .\gradlew.bat bootRun
```

Each language has its own database file (`./data/crm-en`, `./data/crm-ko`), so switching deletes nothing. To start the
sample data over, stop the server, delete that file and start again. Every company and person is made up.

### Turning on the AI assistant (optional)

Keys are read from environment variables only.

```bash
# Claude
CRM_AI_PROVIDER=anthropic ANTHROPIC_API_KEY=... ./gradlew bootRun
# OpenAI or any OpenAI-compatible server (a local LLM, vLLM, ...)
CRM_AI_PROVIDER=openai OPENAI_BASE_URL=http://<host>:8000/v1 CRM_AI_MODEL=<model> OPENAI_API_KEY=<key or anything> ./gradlew bootRun
```

In Windows PowerShell set each variable first (`$env:CRM_AI_PROVIDER='anthropic'`, ...), then run `.\gradlew.bat bootRun`.
Without them the assistant answers "not configured" (503) and everything else works as usual.

## Compared with writing it by hand — what ElastiCORE gives

The numbers below were counted in this repository (non-blank, non-comment lines, on 2026-11-23).
"By hand" means building the same features with Spring Boot and React without a generator.

### 1. What people wrote and what ElastiCORE generated

| Part | Files | Lines | Written by |
|---|---:|---:|---|
| **DSL** (`blueprint/crm/*.yml`) | 8 | **403** | people |
| Server Java (entities, DTOs, mappers, repositories, services, REST, access checks, lifecycles, validation, error handling) | 155 | 12,007 | ElastiCORE |
| TypeScript client (DTOs, API, validation, enum labels) + screen models | 71 | 2,970 | ElastiCORE (+ team rules) |
| Label and message bundles (en/ko) | 4 | 760 | ElastiCORE |
| Database schema document + Ossie semantic model (what the AI assistant reads) | 2 | 1,391 | ElastiCORE |
| Tests: a contract test class per entity and their support class, and the architecture test | 12 | 2,956 | ElastiCORE (written once, then the project's) |
| Team rules `crm-ui-rules` (DSL → screen model, missing-translation check) | 3 | 418 | people, once |
| Hand-written server Java (sample data 545, AI assistant 422, metrics 197, security filters 180, other 172) | 16 | 1,516 | people |
| Screens `web/src` (list, record, form, board, home and assistant, shared by every entity) | 30 | 2,675 (+354 CSS) | people |

- **403 lines of DSL produce about 15,000 lines of Java and TypeScript - about 37 lines per DSL line.**
  With the label bundles, the schema and the semantic model, about 17,000 lines, and about 3,000 lines of tests on top.
- **89%** of the server's Java (12,007 of 13,523 lines) is generated. The hand-written server code is what the DSL
  cannot say: sample data, the AI assistant, metric calculation, CSRF, security headers, sign-in throttling and lead
  conversion.
- The **107 generated REST endpoints** (10 entities with an API) took no hand-written line: read, search, create,
  update, delete, batch operations, lifecycle commands and export.
- The screens know no entity. Only the home dashboard (three entities) and the sign-up page (the sign-in entity's
  generated validator) name one; the lists, records, forms, boards and Excel export of every entity run on the same code.

### 2. Adding one field

What actually changed when **one line** - `segment: string(30) @search(like) @label(en=Segment) -- 고객 세그먼트` - was
added to the DSL and the generation was run:

| | ElastiCORE (measured) | By hand (the places usually edited) |
|---|---|---|
| Edited by hand | **1 DSL line** | DB column, entity, response DTO, search DTO and query, mapper, server and client validation, TS type, list column, search input, form input, detail view, Excel column, two translations, API docs, AI context - more than ten places |
| Files that changed | 14 files, 76 lines (entity, DTO, search DTO, mapper, query class, 3 label files, schema, Ossie model, TS DTO and search DTO, screen model, and a contract test of the field's length) - all generated | one by one, by a person |
| Screen code changed | **0 lines** - the field appears in the list, search, form, detail, Excel and both languages, and the form checks its length | every screen |
| If something is forgotten | no English label: **the build stops**; a screen out of step with the generated client: **the type check fails the build** | found in testing or in production |

`scripts/dsl-change-demo.ps1` runs this end to end: the build stops without the label, the field reaches every place
with it, and a browser test checks the screens.

### 3. Rules kept in one place instead of re-checked everywhere

| Need | DSL | By hand |
|---|---|---|
| Who may do what | `@access(read=REP&owner, answer=REP)`, `@owner(...)` | a check in every service, an "own rows only" condition in every search, the same again in export and metrics - one missing is a data leak |
| Business flow | `states:` (`win: PROPOSAL, NEGOTIATION -> WON`) | state checks, transition APIs, error messages and button states, each written separately |
| Personal data | `@encrypt @s(eq)`, `@env:maskphone` | an encryption converter, an equality index for search, masked answers, and keeping a masked value from overwriting the stored one |
| Several languages | `-- Korean @label(en=...)`, the enum's `label_en` column | translation keys kept apart in the server, the screens, Excel, error messages and the AI context |
| AI that understands the data | the generated Ossie semantic model (field meanings, code meanings in both languages, metric SQL) | a prompt document written by hand and kept in step with every model change |

The rules the server enforces and the rules the screens show come from the same DSL, so a screen cannot offer a button
the server will refuse.

### 4. Security — what the DSL enforces and what was written by hand

By hand, every row below is code of its own, and every new feature means checking again that nothing was missed. The
rows marked ElastiCORE come from one DSL line and apply to every entity and every path the same way.

| Threat / need | Defence | By | Checked by |
|---|---|---|---|
| Reading or changing other people's data | `@access` + `@owner` are checked in every generated service operation (read, search, update, delete, lifecycle commands, export); a search is narrowed to the caller's own rows. Dashboard metrics use the same rules (ElastiCORE's `AccessPolicy`) | ElastiCORE | another rep's account is 404, a customer reading deals is 403, a customer answering a ticket is 403; the generated contract tests check every operation of every entity for each role (401, 403, 404, and another's row missing from a search) |
| Raising one's own role | sign-up is the generated `create=public`, but the `role` field is `@access(admin)` - asking for administrator at sign-up stores a customer | ElastiCORE | sign-up with `role=A` → `C` |
| Password storage and rules | `@password` → PBKDF2 hash, never answered (export included); `@length(min=8)` + `@pattern` (letters and digits) checked by the server | ElastiCORE | a password without a digit is 400 (no password column in the member export: checked by hand) |
| Session theft and fixation | a new session ID at sign-in; a password change ends the other sessions (`@signin(refresh=password)`); cookie HttpOnly, SameSite=Lax, Secure in production, 60 minutes | ElastiCORE + settings | the session ID changes at sign-in |
| Cross-site request forgery | double-submit token: a write must echo the `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header, compared in constant time (`CsrfCookieFilter`) | hand-written | a write without the token is 403 |
| Password guessing | 5 failures per login or 20 per address lock sign-in for 10 minutes; a locked sign-in answers like a wrong password, so it does not reveal whether the login exists (`SignInThrottle`) | hand-written | after 5 wrong passwords the right one is refused too |
| XSS and clickjacking | CSP `script-src 'self'` and `frame-ancestors 'none'`, nosniff, Referrer-Policy, `no-store` on the API (`SecurityHeadersFilter`); React escapes output; the assistant's Markdown renders no HTML | hand-written | CSP and nosniff headers |
| Personal data at rest | `@encrypt` → AES-256-GCM; `@s(eq)` → an HMAC index for equality search only; keys from environment variables, and production does not start without them | ElastiCORE | search by phone number works |
| Personal data in answers | `@Mask` masks the answer, and Excel and the assistant see only that answer; a masked value sent back unchanged keeps the stored one; the Ossie model leaves out passwords and encrypted columns | ElastiCORE | phone and e-mail masked, also in Excel; after a masked round trip the real number is still found |
| Formula injection and bulk leaks in exports | CSV puts `'` before a value starting with `= + - @`, xlsx always writes text cells; more than `elasticore.export.max-rows` (100,000) is 400 | ElastiCORE | `=HYPERLINK(...)` does not become a formula |
| Leaks or SQL injection through the AI assistant | the tools call only the generated services, as the signed-in member (access and masking apply); the LLM writes no SQL, and metric SQL is the generator's, chosen by name; AI keys from environment variables | hand-written (by design) | checked by hand with a local LLM, no automated test |
| Internal details in errors | RFC 9457 problem answers (code and message only), no stack trace, H2 console off; the production profile turns off Swagger, uses `ddl-auto: validate`, creates no demo accounts and has no default secrets | ElastiCORE + settings | - |

Not there yet (to do before production):
- **Audit log:** nothing records who changed what and when.
- **Two-factor authentication** is not there.
- **Sign-in throttling** lives in one server's memory; several servers would need a shared store.
- **Team-level access and "the owner sees the unmasked value"** are requested from ElastiCORE (#178).
- **External testing:** no penetration test and no dependency vulnerability scan (SBOM) have been run.
- **Demo keys:** the encryption keys in `application.yml` are for the demo only. In production set `CRM_CRYPTO_KEY` and
  `CRM_CRYPTO_INDEX_KEY` yourself.

### 5. Costs and limits

- The DSL and its generation rules must be learned, and generated files must not be edited by hand. What the DSL cannot
  say (security filters, the AI assistant, sample data, metric calculation) is still written by hand.
- Building this demo turned up 12 defects and gaps in ElastiCORE. All were reported; 11 are in the released
  ElastiCORE 3, and one (#295, generated tests) is in progress in phases. They were fixed quickly this time, but where
  the framework blocks you, you wait for the fix or report it - that is a cost.
- Generated tests cover lifecycles, field constraints and `@access` so far (#295). Masking and encryption, and the
  export, are still tested by hand (`CrmApiTest`) until the next phases.
- The "by hand" column lists the places usually edited; it is not a timed implementation of the same features.

## What comes from the DSL

```
src/main/resources/blueprint/crm/            ← what people write: env.yml, model/*.yml
        │  ./gradlew build
        ├─ elcore3   → Java: entities, DTOs, services, REST, access (@access/@owner), lifecycles (states:), encryption, masking;
        │              src/test: a contract test per entity (testCode: contract) and the architecture test
        ├─ elcore3ts → src/main/ts: typed client + the screen models of crm-ui-rules (<Entity>Ui.ts)
        ├─ ossie     → blueprint/semantic_model.yml (Apache Ossie): dashboard metrics and the AI assistant's grounding
        ├─ i18n      → crm-labels*.properties (labels, enum values included), crm-messages.properties (refusals)
        └─ webBuild  → web/ type-checked against the generated client and bundled (a mismatch with the DSL fails the build)
```

| Feature | Where | What it does |
|---|---|---|
| Labels in each language | `-- Korean @label(en=English)` | on entity, field and enum lines; enum values from the enum's own `label` and `label_en` columns → the generated `XxxLabel`/`XxxLabels`, the label bundles, Ossie. A missing English label stops the build (`crm.languages` in env.yml) |
| Constraints | `!`, `string(n)`, `@length`, `@min`, `@max`, `@pattern`, `@email`, `@minsize` | checked by the server, by the screens before a request is sent (the generated `validateXxxCodes`), and by the generated contract tests |
| Refusal messages | `labelLanguage: ko` (env.yml) | messages name the field, the entity and the states by their DSL labels ("Opportunity cannot win when Stage is Prospecting; it must be [Proposal, Negotiation]") |
| Menu and screen hints | `@ui(menu=sales, order=20, icon=..., board, title)` | read by `crm-ui-rules` into the screen model |
| Access | `@access(read=REP&owner, create=REP, answer=REP, ...)` `@owner(...)` | roles (`REP`) and levels (`owner`) joined with `&` and `\|`; enforced by the server, and the screens and metrics read the same rules |
| Lifecycles | `states:` | the record's step bar, drag and drop on the board, the command buttons |
| Metrics | `metrics:` | exported to Ossie; the server runs their SQL within the member's access for the dashboard and the assistant |
| Personal data | `@encrypt`, `@env:maskphone` | stored with AES-GCM, answered masked; a masked value sent back unchanged keeps the stored one; Excel and the assistant see only masked values |
| Export | `@expose(20, methods="...,export")` | ElastiCORE's `POST .../export` (the search's access and masking, .xlsx or CSV, at most `elasticore.export.max-rows`) |

### Screens (web/)

Vite + React 19 + **Refine 5 + Ant Design 5**. The screen code knows no entity: `import.meta.glob` collects every
`*Ui.ts` and builds the menu, lists, records, forms and boards from them.

- **Table grid:** a real HTML `<table>` (Ant Design Table). Paging, sorting and searching run on the server
  (`pageNumber`, `pageSize`, `sortCode` of `POST /search`); the state stays in the address, so a reload or a shared
  link shows the same list. Shown columns, order, widths and row height are kept per user.
- **Two Excel exports:** "the table as shown" (in the browser with exceljs - the visible columns, order and widths) and
  "every matching row" (ElastiCORE's generated `POST .../export` - every row of the same search, same access and
  masking, up to 100,000 rows). Headers and enum values (Prospecting / 발굴) follow the chosen language, and values that
  look like formulas stay text. Enum labels come from the export request's `labels: "*"` (ElastiCORE #289).
- **Forms check what the DSL declares before sending:** every constraint through ElastiCORE's generated
  `validateXxxCodes`, each code said in the chosen language (`web/src/form/codes.ts`). The sign-up page uses the
  sign-in entity's validator, so the password rule lives in the DSL only. A field error from the server lands on its
  field, a quote line's (`lines[0].quantity`) on its row.
- Record pages (summary, steps, related lists, activity timeline), boards (only the moves the DSL's `states:` allow),
  a dashboard (the DSL's metrics) and the AI assistant.

## Tests

| Suite | Tests | What it checks | Written by |
|---|---:|---|---|
| `<Entity>ContractTest` (10 classes) | 362 | over HTTP, by status and problem code (never wording): each lifecycle move is allowed from its states and refused from the others (409 `state.*`), and a `@stamp` is set. Every `!`, length, `@min`, `@max`, `@minsize`, `@pattern`, `@notblank` and `@email` boundary passes, while one past it is refused (400, the field and its code). For `@access`, on each operation a caller each term lets in passes, a visitor gets 401, another role 403 and another's row 404 or no search hit | ElastiCORE (`testCode: "contract"`), 361 generated; 1 added by hand: a quote line's quantity (a member's own constraint) |
| `CrmArchitectureTest` | 3 | the hexagon's layer boundaries (ArchUnit), the hand-written packages included | ElastiCORE (`archTest: true`) |
| `CrmApiTest` | 9 | over HTTP: the generated export, roles in `@access`, masked round trip, labelled refusals, metrics, CSRF | people |
| `MessageBundleTest` | 2 | the label and message bundles in both languages | people |
| `SecurityFiltersTest` | 8 | the CSRF guard and the security headers | people |
| `SignInThrottleTest` | 5 | password-guessing locks per login and per address, their expiry and reset | people |
| `SeedTextTest` | 7 | the English and Korean sample data have the same shape, and every code they name exists | people |
| `AppInfoControllerTest` | 3 | the currency the screens show | people |
| `CrmUiRulesTest` (crm-ui-rules) | 4 | the team rule: DSL → screen model, a missing English label stops the build, blind indexes are skipped | people |

403 tests in all under Gradle, none skipped, plus 10 Playwright screen tests and 29 smoke checks of the API.

The contract tests are written once by ElastiCORE and then belong to the project: a DSL change is merged into them, and
the edits are kept. Every request goes through `CrmContractSupport`, which adds the demo's XSRF cookie and header,
the only change the generated tests needed. Its `signIn(role)` signs in a member of each role through the generated
sign-in. Tests of masking and encryption, and of the export, come in the next phases of ElastiCORE #295; until then
`CrmApiTest` checks them by hand.

```bash
./gradlew test                                   # every suite above (results: build/test-results)
powershell -File scripts/run-local.ps1           # starts the jar on port 18090 (Windows PowerShell)
powershell -File scripts/smoke-api.ps1           # 29 API calls made the way the browser makes them
cd web && CRM_URL=http://localhost:18090 npx playwright test   # screen tests in the installed Chrome
powershell -File scripts/dsl-change-demo.ps1     # add a field to the DSL → it reaches everything (then undone)
```

## Coding with AI — guide and pitfalls

This demo was built with an AI coding agent (Claude Code). Below is what worked and the pitfalls it actually hit. It
applies both when a person hands work to an AI and when an AI changes this repository.

### Guide — in this order

1. **First see whether the DSL can say it.** Fields, access, lifecycles, derived values, labels and metrics change in
   the YAML under `src/main/resources/blueprint/crm/`. Write by hand only what the DSL cannot: an extension point such
   as a service's `beforeSave`, or a package of its own placed in a layer of the hexagon (`application.security`,
   `adapter.in.web.security`, `adapter.in.assistant`, `adapter.in.seed`, `infrastructure.semantic`) so the generated
   architecture test checks it too.
2. **Take the syntax from the project's guides, not from memory.** Every generation copies the guides of the version
   in use to `blueprint/elasticore-*.md`. Find the section in `elasticore-guide-index.md` by a word (`@access`,
   `states:`, `export`) and read only those lines. Syntax changes between versions (while this demo was built, the
   enum label column went from `labelEn` to `label_en`).
3. **Generate → read the warnings → compile → test, in that order.**
   - Bring the DSL warnings in the output of `./gradlew elcore3 elcore3ts` to zero. Generation reports success even
     with warnings, so read them.
   - Then `compileJava` → `test` → `npx tsc -b` for the screens → `smoke-api.ps1` → Playwright.
4. **Screens do not name entities.** A new screen feature reads the generated screen model
   (`src/main/ts/<Entity>Ui.ts`). If the screen model lacks something, add it to the team rules `crm-ui-rules`, not to a
   screen.
5. **Give every new field, entity and enum value its English label from the start** (`@label(en=...)`, the enum's
   `label_en`). Without it the build stops - on purpose.
6. **Have results reported in business terms** - who can now do what, what is refused in which state - with the test
   results as evidence.
7. **Do not work around ElastiCORE.** Write the smallest DSL, the code it produced and what was expected into
   `docs/elasticore-issues/` and file an issue. 11 of the 12 reported this way are fixed, and the demo's work-arounds are gone.

### Pitfalls this project actually hit

| Pitfall | What happened | What to do |
|---|---|---|
| Editing generated files | overwritten by the next generation, or a merge conflict | change files listed in `.elasticore/cache` through the DSL; commit the cache and never edit or delete it |
| Files rewritten on every generation | `crm-messages.properties`, `schema.yml` and `semantic_model.yml` are written anew each time | put translations in the `_ko` copy |
| Access decided separately in the screens | screen and server rules drift apart | the screens (`accessControl.ts`) and metrics (`AccessPolicy`) read the same `@access` expressions; the server decides |
| A test that passed without checking anything | turning `$431,000` into a number gave NaN, and the sort test compared nothing | look at what a test really compares; judge results by failures/errors in `build/test-results/test/TEST-*.xml`, not by an exit code after a pipe |
| A hidden field in the rule API | the index field of `@encrypt` (`phoneBidx`) reached the team rules like a declared field and showed up in the form (#290; now skipped by `FieldSpec.blindIndexOf()`) | after changing the screen model, look at the real form and record page in a browser |
| A form validation rule | an Ant Design required rule with any extra key validates the value as a string, so a numeric reference failed as "required" | let the Playwright tests save the form, not just open it |
| Hard-coded text | the lead conversion added a Korean suffix to deal names in the English data | take text people read from DSL labels or the language setting |
| Secrets | AI keys, database passwords and encryption keys could end up in the repository | read them from environment variables only; the keys in `application.yml` are for the demo |
| Extending the AI assistant | giving the LLM SQL or the database skips access checks and masking | let the tools call the generated services only, as the signed-in member |
| Running the server | `gradlew bootRun` never returns in an agent's shell, and killing processes by name kills other Java processes | `scripts/run-local.ps1` (the jar in the background, its PID recorded) and `stop-local.ps1` (that PID only) |
| Windows | use `gradlew.bat`; a `.ps1` without a BOM garbles Korean in Windows PowerShell 5.1 | save scripts as UTF-8 with a BOM |
| Data left by tests | the smoke and e2e tests leave records in the demo database | before a demo, delete `data/crm-<language>.mv.db` and start again |
| Bounds copied into the screens | a sign-up form that repeats the DSL's password rule drifts from it when the DSL changes | check in the browser with ElastiCORE's generated `validateXxxCodes` and say each code in the screen's words (`web/src/form/codes.ts`) |

Instructions that gave steady results when handing work to an AI:
"If the DSL can do it, use the DSL; if not, say why. Show the generation warnings, compilation, tests and screen tests
as evidence. Do not edit generated files, and report ElastiCORE problems instead of working around them."

## Reported to ElastiCORE

The defects and improvement requests found along the way are in [docs/elasticore-issues](docs/elasticore-issues/README.md),
written in ElastiCORE's issue form. The first nine (#266-#274) are in the released ElastiCORE 3, and the demo uses them
instead of its own work-arounds (a staff-only check, its own Excel export, its own enum label rule). The next two
(#289, enum labels in the export; #290, the index field in the rule API) are in it too and used here. In progress:
#295, tests generated from the DSL. Lifecycles, constraints and `@access` are in, and the demo uses them; masking,
encryption and the export are to come.

## Background

The feature scope follows [jmix-crm](https://github.com/jmix-framework/jmix-crm), but no code was taken from it; the
business focus (deals and customer service), the screen flow and the design are this project's own. Development plan
(Korean): [docs/development-plan.md](docs/development-plan.md).
