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

## Create and detail flow

The create page validates title and content, optionally loads owned tags, and
posts only `title`, `content`, and `tagIds`. After creation, TanStack Query seeds
the new journal's detail cache, invalidates only list caches, and navigates to
`/journals/{id}` without an unnecessary detail request.

Opening a journal directly requests `GET /api/v1/journals/{id}`. The backend
enforces ownership; a missing or unowned ID is displayed as the same safe
not-found state and does not reveal whether another user owns that ID.

Edit and delete workflows remain separate in Phase 1.10C.
