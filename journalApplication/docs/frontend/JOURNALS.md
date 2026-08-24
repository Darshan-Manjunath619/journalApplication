# Frontend Journals

## Dashboard list flow

```text
DashboardPage
  -> useJournals({ page, size })
  -> TanStack Query cache key
  -> GET /api/v1/journals?page=...&size=...&sort=createdAt,desc
  -> PageResponse<Journal>
  -> JournalCard list and pagination controls
```

The API returns a bounded page rather than every journal row. TanStack Query
caches each page independently and keeps the previous page visible while the
next page loads. The dashboard provides distinct loading, empty, error/retry,
and populated states.

Phase 1.10A is read-only. Create, view, edit, and delete workflows are delivered
as separate increments so each API mutation and cache update can be verified.
