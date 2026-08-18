# Phase 1.5A — Journal Favorites

## Why

Favorites give users a simple way to mark important journal entries without
creating another entity or workflow.

## Example

```http
PATCH /api/v1/journals/10
Content-Type: application/json

{"favorite": true}
```

The existing owner-scoped update ensures only the journal owner can change it.

```http
GET /api/v1/journals?favorite=true
```

This returns only the authenticated user's favorite journals.

## Database

Flyway V4 adds a non-null `favorite` boolean with a default of `false` and an
index on `(user_id, favorite, created_at)` for owner-specific favorite lists.

## Flow

```text
favorite query parameter -> JournalSearchCriteria
-> owner + favorite JPA predicates -> Hibernate SQL -> PageResponse
```

New journals start as non-favorites. `PATCH` can change only `favorite`, or can
change it together with title/content. Invalid and cross-user journal IDs retain
the existing Problem Detail and `404` behavior.
