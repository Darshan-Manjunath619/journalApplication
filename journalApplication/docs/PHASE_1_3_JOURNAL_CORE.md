# Phase 1.3 — Journal Core

## Why

Journal behavior needed a stable `/api/v1` contract, database-backed ownership
checks, and timestamps that show when each entry was created and changed.

## API

```text
GET    /api/v1/journals
POST   /api/v1/journals
GET    /api/v1/journals/{id}
PATCH  /api/v1/journals/{id}
DELETE /api/v1/journals/{id}
```

## Ownership example

```text
Alice requests entry 10
→ query uses id=10 and username=alice
→ Alice owns it: return entry
→ Bob owns it or it is missing: return 404
```

Returning the same `404` for missing and unowned entries prevents revealing
another user's journal IDs.

## Request flow

```text
Bearer JWT → Spring Security → JournalsController
→ validated DTO → transactional JournalEntryService
→ owner-scoped repository query → MySQL
→ JournalMapper → safe JournalResponse
```

Empty collections return `200 []`. Create returns `201`, delete returns `204`,
and invalid requests use the existing Problem Detail contract.

## Database change

Flyway V3 adds:

```text
journal_entries.created_at
journal_entries.updated_at
index(user_id, created_at)
```

Spring Data JPA auditing fills UTC `Instant` values automatically. The old
`date` field and `/journal` route remain temporarily for compatibility; new
clients use the audit fields and `/api/v1/journals`.

## Production note

The `(user_id, created_at)` index supports owner-specific chronological reads.
Pagination and richer filtering are intentionally handled in Phase 1.4.
