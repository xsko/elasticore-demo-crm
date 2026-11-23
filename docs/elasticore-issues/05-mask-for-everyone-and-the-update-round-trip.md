# `@Mask` hides the value from its owner too, and a form that sends it back writes the mask

- **Version:** ElastiCORE 3 (`elasticore-springboot4`), `@dto:...Mask(MaskType.PHONE)` via `env.yml`
- **Kind:** feature request

The env guide says it plainly: everyone gets the masked value, the owner and the administrator
too, and "a client that sends a read DTO back unchanged writes the masked text: send a masked field
only when it changes". That makes two things hard for a CRM:

1. The account owner calling a contact cannot see the contact's number anywhere.
2. Every generic edit form must remember which fields are masked and leave them out of the
   update - otherwise `010-****-1234` overwrites the stored number (with `@encrypt`, it is
   encrypted and stored as the new value).

## Smallest model

```yaml
# env.yml: annotations: { maskphone: "@dto:io.elasticore.springboot3.mask.Mask(io.elasticore.springboot3.mask.MaskType.PHONE)" }
entity:
  Contact:
    meta: entity @root @editable @expose(20) @owner(ownerMemberId) @access(read=owner, update=owner)
    fields:
      contactId: long @id @sequence
      phone: string(20) @encrypt(digits) @s(eq) @env:maskphone
      owner: Member @ManyToOne
```

## Expected (either)

- a level that sees the plain value: `@mask(PHONE, except=owner)` / `except=admin`
- an update that ignores a value equal to what the answer showed (the masked text), so a form
  can send the read DTO back

## What the demo does meanwhile

The form leaves masked fields empty on edit and sends them only when typed.
