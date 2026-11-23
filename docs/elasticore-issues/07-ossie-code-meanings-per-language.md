# Ossie export: code meanings in the model's language only

- **Version:** ElastiCORE 3, `rules = ['ossie-export']`
- **Kind:** feature request

The field labels' translations reach `ai_context.synonyms` (good: an English question finds the
Korean field). An enumeration's codes are described from its first label column only:

```yaml
- name: stage
  ai_context:
    instructions: "Codes: PRS = 발굴, PRP = 제안, NGT = 협상, WON = 수주, LST = 실주"
```

An AI assistant answering in English shows `발굴` or the bare code; it has nothing to say
"Prospecting" from. The enum's other columns (`labelEn` in the demo) and the enum's own
`@label(en=...)` are not exported.

## Expected

The code meanings in each language the model has - for instance
`"Codes: PRS = 발굴 (en: Prospecting), ..."` or a structured `codes:` list in the ELASTICORE extension.
