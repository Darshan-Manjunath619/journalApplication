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

## Edit and delete flow

The edit page loads the owned journal and available tags, then pre-fills the
shared journal form. Saving sends title, content, favorite, and complete tag
assignments to `PATCH /api/v1/journals/{id}`. The response replaces the detail
cache and invalidates list caches so both pages reflect the saved values.

Delete is available from the detail page only after explicit confirmation.
`DELETE /api/v1/journals/{id}` removes the detail cache, refreshes journal
lists, and returns the user to the dashboard. The backend remains responsible
for ownership and returns the same `404` for missing or unowned journals.

## Search and sorting

The dashboard submits search text as `q` and sends only the backend allowlisted
sort fields (`createdAt`, `updatedAt`, or `title`) with `asc` or `desc`. Search
is submitted explicitly instead of querying on every keystroke. Changing search
or sort returns pagination to page zero, and the full request becomes part of
the TanStack Query cache key.

## Filters and URL state

Tag, favorite, start-date, and end-date filters are stored with search, sort,
and page in the dashboard URL. For example, `/dashboard?tag=3&favorite=true`
restores the same controls after a refresh. The URL values are validated before
they become a typed journal request; invalid values fall back to safe defaults.

The service converts date-only controls into UTC day boundaries before calling
the backend. Clearing filters removes only filter parameters, preserves search
and sorting, and returns pagination to page zero.
