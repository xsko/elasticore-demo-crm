# `@access` cannot name a role other than the administrator

- **Version:** ElastiCORE 3, `architecture = 'hexagonal'`, `@signin` with a `role` enum
- **Kind:** feature request

The levels are `public`, `user`, `owner`, `admin` and `none`. An application with more than one
kind of signed-in member cannot say who of them may do something:

- **Customers and staff sign in through the same entity** (one `@signin` entity per application).
  `create=user` on `Account` lets a customer who signed up for support create sales accounts.
- **A manager who reads the team's rows.** `read=owner` gives the owner and the administrator;
  a sales manager in between has no level.

## Smallest model

```yaml
enumeration:
  MemberRole:
    meta: enum @db(code) @json(code)
    fields: { code: string(1) @code, label: string(20) }
    enum: { ADMIN: A, Administrator, MANAGER: M, Manager, REP: R, Sales rep, CUSTOMER: C, Customer }
entity:
  Member:
    meta: entity @root @editable @expose(20) @signin @owner(memberId) @access(read=owner, create=public, update=owner)
    fields:
      memberId: long @id @sequence
      login: string(30)! @login
      password: string(255)! @password
      role: MemberRole @default(MemberRole.CUSTOMER) @access(admin)
  Account:
    meta: entity @root @editable @expose(20) @owner(ownerMemberId) @access(read=owner, create=user, update=owner)
    fields:
      accountId: long @id @sequence
      name: string(100)!
      owner: Member @ManyToOne
```

## Expected

A level naming roles, for example `@access(read=owner|MANAGER, create=REP|MANAGER)` - the role
values the session already holds (`SessionCurrentUser` keeps the role, `hasRole` exists).

## Actual

No such level; an unknown level is a DSL warning. The demo calls a check of its own in
`beforeSave` of six services (`StaffOnly.require`) and gives up the manager role.
