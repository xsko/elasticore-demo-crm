# `@pattern` whose regular expression has parentheses is cut silently

- **Version:** ElastiCORE 3, `architecture = 'hexagonal'`, Spring Boot 4.0.8, Java 17
- **Kind:** bug - wrong validation, no warning

## Smallest model

```yaml
entity:
  Member:
    meta: entity @root @editable @expose(20)
    fields:
      memberId: long @id @sequence
      password: string(255)! @pattern('^(?=.*[A-Za-z])(?=.*[0-9]).+$', 'password needs a letter and a digit')
```

## Expected

```java
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).+$", message = "password needs a letter and a digit")
```
or, if parentheses inside a quoted value are not supported, a DSL warning naming the field.

## Actual

```java
@Pattern(regexp = "digit'", message = "비밀번호 is not in the expected form")
```

The regular expression becomes the tail of the message (`digit'`), and the message becomes the
default sentence. The build succeeds with no DSL warning. Every value is then refused (`demo1234`
fails), and the TypeScript validator gets the same wrong pattern. The parser seems to end the
annotation's argument list at the first `)` inside the quoted regular expression.

Lookaheads (`(?=...)`), groups and alternation in parentheses are all common in password and code
rules, so this is easy to hit.

## Workaround in the demo

A regular expression without parentheses:
`@pattern('^.*[A-Za-z].*[0-9].*$|^.*[0-9].*[A-Za-z].*$', 'password needs a letter and a digit')`
