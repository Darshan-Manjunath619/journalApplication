# Phase 2 Service Boundaries

## Boundary rule

A service owns a business capability, its write rules, and its database tables.
Another service cannot import its entities or query its tables.

## Identity Service

Responsibilities:

- Registration and BCrypt password verification
- Login, access-token signing, refresh rotation, and logout
- User profile and password changes
- Roles and administrator user listing
- Refresh-token revocation and signing-key ownership

Owned routes:

```text
POST  /api/v1/auth/register
POST  /api/v1/auth/login
POST  /api/v1/auth/refresh
POST  /api/v1/auth/logout
GET   /api/v1/users/me
PATCH /api/v1/users/me
PATCH /api/v1/users/me/password
GET   /api/v1/admin/users
```

Owned code moves toward `identity` packages but remains the original application
during the first extraction.

## Journal Service

Responsibilities:

- Journal CRUD and user ownership
- Search, filtering, sorting, and pagination
- Favorites and audit timestamps
- Tag CRUD, normalization, assignment, and conflict rules
- Journal-specific Flyway migrations and OpenAPI documentation

Owned routes:

```text
GET/POST          /api/v1/journals
GET/PATCH/DELETE  /api/v1/journals/{id}
GET/POST          /api/v1/tags
PATCH/DELETE      /api/v1/tags/{id}
```

Journals and tags stay together because journal updates and tag assignments need
one local transaction. Making Tag Service separate would add network calls and
distributed consistency without an independent scaling need.

## Shared code policy

Services may duplicate small stable infrastructure concepts:

- Problem Detail shape
- Correlation-ID header name
- Pagination response contract
- JWT claim names and validation rules

They must not share:

- JPA entities
- repositories
- database migrations
- application services
- a common deployable library containing business rules

Small duplication is safer than a shared business library that forces both
services to release together.

## Communication matrix

| Caller | Callee | Reason | Frequency |
|---|---|---|---|
| SPA | Identity | Auth, profile, admin | User-driven |
| SPA | Journal | Journal and tag workflows | User-driven |
| Journal | Identity | None on normal request path | Never |
| Identity | Journal | Future idempotent account-data purge | Rare |

User deletion is not currently part of the supported versioned API. Before it is
introduced, the design must use an idempotent purge workflow rather than a
cross-database transaction.

## Service contract rules

- Public APIs remain `/api/v1` and use existing DTO shapes during extraction.
- Each service publishes its own OpenAPI document.
- HTTP timeouts are mandatory for any internal call.
- Automatic retries apply only to idempotent operations.
- Internal endpoints require service authentication and are not browser-accessible.
- Correlation IDs are forwarded across service calls.

## Why not more services

| Candidate | Decision | Reason |
|---|---|---|
| Tag Service | Keep with Journal | Same owner and transaction boundary |
| Notification Service | Defer | No completed independent notification workflow |
| Weather Service | Defer/remove | Optional widget, not core business capability |
| Admin Service | Keep with Identity | Reads identity-owned user data |
| Search Service | Defer | MySQL search is sufficient and measured need is absent |
