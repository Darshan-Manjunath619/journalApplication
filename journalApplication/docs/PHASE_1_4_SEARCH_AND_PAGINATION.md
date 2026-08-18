# Phase 1.4 — Search and Pagination

## Why

Returning every journal does not scale. This phase limits each response and lets
the database search, filter, and sort only the authenticated user's entries.

## Example

```http
GET /api/v1/journals?page=0&size=20&q=spring&sort=createdAt,desc
```

This returns the first 20 journals containing `spring`, newest first.

## Parameters

```text
page  zero-based page number, default 0
size  entries per page, 1 to 100, default 20
q     case-insensitive title or content search
from  minimum createdAt as an ISO-8601 UTC instant
to    maximum createdAt as an ISO-8601 UTC instant
sort  createdAt, updatedAt, or title plus asc/desc
```

Favorites and tags are deferred until their domain fields are added in Phase 1.5.

## Flow

```text
query parameters -> controller validation -> search criteria
-> owner-scoped JPA specification -> Hibernate SQL
-> MySQL WHERE / ORDER BY / LIMIT / OFFSET -> PageResponse
```

Hibernate generates parameterized SQL at runtime. The authenticated username is
always part of the specification, so one user cannot search another user's data.

## Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Unsupported sorting, invalid dates, negative pages, sizes outside 1–100, and a
`from` date after `to` return `400` using the existing Problem Detail contract.

## Production consideration

Offset pagination is simple and appropriate at the current scale. A stable ID
tie-breaker prevents rows with equal sort values from moving unpredictably
between pages. Cursor pagination should be considered only if measured large
offsets become slow.
