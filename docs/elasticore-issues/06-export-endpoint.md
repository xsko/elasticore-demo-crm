# A generated export (Excel / CSV) of a search

- **Version:** ElastiCORE 3
- **Kind:** feature request

Nearly every business list has an "export" button. Done right it is not trivial, and every
project writes it again:

- the same search DTO as `POST /search`, every page of it, with the same `@access` / `@owner`
  filter and `@Mask` (or the file leaks what the screen hides);
- headers in the request's language - the label bundles of ElastiCORE 3 already have them;
- enum values by label, money and dates as numbers and dates, not text;
- cells starting with `= + - @` kept as text (formula injection);
- a row limit, streamed writing.

## Suggestion

`@expose(export)` (or `@expose(methods="...,export")`) generating
`POST /api/<domain>/<entity>/export` with `{ search, sortCode, columns }` and an `.xlsx` (and/or CSV)
answer, written through the generated use case.

## What the demo does

`ExcelExportService` + `ExportController` (`POST /api/crm/export/{Entity}`): finds
`<Entity>UseCase.findBySearch` by name, pages through it as the signed-in member, writes with POI's
`SXSSFWorkbook`, headers from `crm-labels*.properties`. About 250 lines that any project could get
from the DSL.
