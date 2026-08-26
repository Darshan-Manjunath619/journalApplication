# Phase 1 Architecture

## System overview

Phase 1 is a modular monolith: one Spring Boot deployment and one MySQL
database, with business responsibilities separated inside the codebase. The
React SPA is a separate application that depends only on the HTTP contract.

```text
React SPA
  |
  | HTTPS/JSON, /api/v1
  v
Spring Security filters
  |
  v
DTO controllers
  |
  v
Services and transactions
  |
  v
JPA repositories
  |
  v
Hibernate + Flyway + MySQL
```

This keeps deployment and database transactions simple while creating boundaries
that can be evaluated for extraction during Phase 2.

## Repository structure

```text
journalApplication/
|-- src/main/java/.../
|   |-- auth/          access and refresh-token lifecycle
|   |-- user/          profile DTOs and mapping
|   |-- journal/       journal DTOs, mapping, and search criteria
|   |-- tag/           user-owned tag domain
|   |-- controller/    versioned HTTP boundary
|   |-- service/       application rules and transactions
|   |-- repository/    JPA persistence interfaces
|   |-- entity/        user and journal persistence models
|   |-- config/        security, OpenAPI, Redis, and time configuration
|   |-- filter/        JWT authentication
|   `-- shared/        errors, pagination, and correlation IDs
|-- src/main/resources/
|   `-- db/migration/ versioned Flyway SQL
|-- frontend/
|   |-- src/app/       router, providers, and Query client
|   |-- src/features/  auth, profile, journals, and tags
|   |-- src/pages/     route-level screens
|   |-- src/components reusable UI
|   |-- src/lib/       shared API client
|   `-- src/test/      frontend test setup
|-- docs/
|-- pom.xml
`-- README.md
```

Controllers accept validated request DTOs and return response DTOs. JPA entities
remain inside the backend and are never serialized as the public contract.

## Authentication flow

```text
Login credentials
  -> Spring AuthenticationManager verifies BCrypt password
  -> backend returns signed access JWT
  -> backend sets opaque refresh token cookie
  -> only refresh-token hash is stored in MySQL
```

The frontend keeps the access token in memory and sends it in the
`Authorization` header. When it expires, one shared refresh request rotates the
HttpOnly cookie and the original request is retried once. Logout revokes the
refresh token.

## Journal request flow

Example: create a journal.

```text
Journal form
  -> Zod validates browser input
  -> POST /api/v1/journals with access token
  -> JWT filter authenticates the user
  -> controller validates CreateJournalRequest
  -> service verifies tag ownership and starts transaction
  -> repository persists through Hibernate
  -> MySQL commits
  -> JournalResponse returns
  -> TanStack Query refreshes the dashboard data
```

Every journal and tag query is scoped by the authenticated username or user ID,
so another user's resource behaves as not found.

## Database structure

```text
users 1 -------- * journal_entries
users 1 -------- * user_roles
users 1 -------- * refresh_tokens
users 1 -------- * tags
journal_entries * -------- * tags
                 journal_entry_tags
```

Main tables:

- `users` and `user_roles`: identity, password hash, and roles.
- `journal_entries`: owned content, audit timestamps, and favorite flag.
- `tags`: normalized user-owned reusable names.
- `journal_entry_tags`: many-to-many association.
- `refresh_tokens`: token hashes, expiry, revocation, and creation time.
- `flyway_schema_history`: applied migration versions and checksums.

Flyway owns schema changes. Hibernate uses `ddl-auto=validate` to detect a
mismatch between Java mappings and the migrated schema.

## Frontend state

- TanStack Query manages remote server state and cache invalidation.
- React context manages the in-memory authenticated session.
- React Hook Form and Zod manage form state and validation.
- URL query parameters preserve dashboard search, filters, sorting, and page.
- Local component state handles temporary UI behavior.

A general Redux store is unnecessary because no cross-application client state
currently justifies it.

## Runtime configuration

Backend secrets come from environment variables or the ignored local `.env`.
Frontend `VITE_*` variables are public build-time settings. Local development
allows `http://localhost:5173` through CORS; production must list the actual
HTTPS frontend origin.

Weather, Redis, mail, and sentiment scheduling are optional integrations and do
not participate in core journal CRUD startup or health.
