# Export: an enumeration written by its label in the request's language

- **Version:** ElastiCORE 3, `architecture = 'hexagonal'`
- **Kind:** feature request (follows #271 and #268)

The export of #271 writes an enumeration by its code, as the JSON answer has it:

```yaml
enumeration:
  OpportunityStage:
    meta: enum @db(code) @json(code)
    fields:
      code:     string(3) @code
      label:    string(20)
      label_en: string(30)
    enum:
      PROSPECT: PRS, 발굴, Prospecting
      PROPOSAL: PRP, 제안, Proposal
entity:
  Opportunity:
    meta: entity @root @expose(20, methods="create,read,search,update,delete,export")
    fields:
      stage: OpportunityStage @label(en=Stage) -- 단계
```

`POST /api/crm/opportunity/export` with `Accept-Language: ko` titles the column `단계` (from the
label bundle) but writes `PRS`, `PRP` in its cells. The screen the user exported from shows
`발굴`, `제안`; a sales manager opening the file sees codes that mean nothing to them.

## Expected

An opt-in that writes an enumeration by its label in the request's language - the same language
the headers already take - for example `"values": "labels"` in the body (or a word in `@expose`):

- `ko`: the label column (`발굴`), `en`: `label_en` (`Prospecting`), else the label column;
- the code stays the default, so a file read back by a program is unchanged.

The labels are already there since #268 (`Enum.CODE` keys in the label bundle per language).
#271 asked for "enum values by label"; its final spec and the export that shipped write the code,
which is the right default - this asks for the label as the option beside it.

## What the demo does meanwhile

The "every matching row" button uses the generated export as it is (codes); the "table as shown"
button writes the labels in the browser.
