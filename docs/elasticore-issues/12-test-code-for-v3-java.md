# `testCode` for the v3 Java bundles: tests of what the DSL declares

- **Version:** ElastiCORE 3, `architecture = 'hexagonal'`, Java
- **Kind:** feature request

`elasticore-env-guide.md` 6.2 says `testCode: "true"` writes JUnit 5 service tests for the 2.x output and Kotlin; the
v3 Java bundles (layered, ddd, hexagonal) and MyBatis write none. The only test the v3 Java bundle writes is the
architecture test (`archTest: true`), which this demo uses.

The demo's DSL declares rules that each need a test written by hand today:

```yaml
Opportunity:
  meta: entity @root @expose(20, methods="create,read,search,update,delete,export") @owner(ownerMemberId)
        @access(read=REP&owner, create=REP, update=REP&owner, win=REP&owner, lose=REP&owner)
  fields:
    amount:      money(15.2) @min(0)
    probability: int @min(0) @max(100)
    stage:       OpportunityStage
  states:
    stage:
      initial: PROSPECT
      propose: PROSPECT -> PROPOSAL
      win:     PROPOSAL, NEGOTIATION -> WON
```

We wrote integration tests (MockMvc) for a handful of them - a customer is refused the deals, a win from PROSPECT is
refused, a masked phone sent back keeps the stored number - and the rest of the 11 entities' rules are untested.

## Expected

`testCode: "true"` for the v3 Java bundles, written once and kept in step like the architecture test, with tests of
what the model declares, run against the generated services (or the REST API):

- each `@access` entry: a caller it lets in passes, one it does not gets 401/403; `owner` narrows the search to the
  caller's rows; a role entry (`REP&owner`) refuses another role;
- each `states:` move: allowed from the states it lists, refused from the others (409, with the labelled message of
  #274); a `@stamp` field is set on entering its state;
- each invariant and constraint (`@min`, `@max`, `@length`, `@pattern`, `!`): the boundary passes, one past it is refused
  with its code;
- `@Mask` and `@encrypt @s(eq)`: the answer is masked, a masked value sent back keeps the stored one (#270), the stored
  column is not the plain value, and an equality search finds the row;
- `export` (#271): the same rows as the search, `max-rows` refused before anything is written.

The DSL is the specification; tests generated from it would catch a regression in the generator or in a hand-written
`beforeSave`, and give an AI agent that edits the model an immediate, precise signal.
