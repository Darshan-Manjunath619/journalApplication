# Phase 1.5B — User-Owned Tags

## Why

Tags are reusable labels that let a user organize one journal under several
topics, such as `java`, `spring`, and `work`.

## API

```text
GET    /api/v1/tags
POST   /api/v1/tags
PATCH  /api/v1/tags/{id}
DELETE /api/v1/tags/{id}
```

Journal create and update requests accept `tagIds`. Journal responses contain
safe tag DTOs, and `GET /api/v1/journals?tag={id}` filters by a tag.

## Example

```text
Create " Spring " -> stored display name "Spring", normalized name "spring"
Create "spring" again for the same user -> 409 Conflict
```

Names are unique per user, so Alice and Bob may each own a `spring` tag.

## Ownership flow

```text
authenticated username + requested tag IDs
-> load only tags owned by that username
-> every ID resolved: assign
-> any ID missing or owned by another user: 404
```

An assigned tag cannot be deleted. The API returns `409` until the user removes
it from all journals. This prevents accidental loss of organization data.

## Database

Flyway V5 adds `tags` and `journal_entry_tags`. A unique constraint enforces
`(user_id, normalized_name)`, while indexes support user tag lists and reverse
journal/tag lookup.

Tags are loaded inside the service transaction and batched before DTO mapping,
so API responses do not depend on Open Session in View and avoid one query per
journal in normal page sizes.
