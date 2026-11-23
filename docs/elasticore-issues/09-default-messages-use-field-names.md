# Generated messages name the field, not its label

- **Version:** ElastiCORE 3, `architecture = 'hexagonal'`
- **Kind:** feature request

`crm-messages.properties` (generated) says:

```
invariant.Opportunity.probability.max=probability must be at most 100
invariant.Account.annualRevenue.min=annualRevenue must be at least 0
state.Opportunity.stage.win=Opportunity cannot win when stage is {0}; it must be {1}
```

ElastiCORE 3 knows each field's and entity's label in every language (`crm-labels*.properties`), but
the default sentences use the code names (`annualRevenue`, `Opportunity`) and the states' codes.
Every application translates every sentence by hand to get "Annual revenue" / "연 매출".

## Expected

Default sentences built from the labels in the request's language (`{label} must be at least 0`,
the states by their enum labels), so only the wording needs a translation, not the names.
