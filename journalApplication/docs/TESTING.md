# Backend Testing Strategy

## Goal

The backend test suite must verify critical rules without MySQL, Redis, SMTP,
Weatherstack, or developer credentials running on the machine.

## Test levels

| Level | Purpose | Current examples |
|---|---|---|
| Unit | One class with mocked dependencies | token rotation, profile rules, journal ownership |
| Web/controller | HTTP validation and response shape | auth, users, journals, Problem Detail |
| Integration | Security through JPA and database | authentication flow, journal CRUD/search, tags |
| Migration | Flyway schema history and objects | migrations V1 through V5 |

The integration profile uses an in-memory H2 database in MySQL compatibility
mode. Flyway creates the schema and Hibernate validates it. Redis repositories
and the sentiment scheduler are disabled. Test values are non-production
fixtures only.

Phase 1.6C-1 adds repository-level checks for owner-scoped combined filters,
stable pagination when primary sort values tie, database-enforced tag
uniqueness, and transaction rollback after a failed journal update.

Example: an update first changes a journal title and then fails because a tag
does not exist. The integration test reloads the journal and proves that the
title change was rolled back, so the transaction is atomic.

## Commands

Run the final backend verification, including packaging:

```powershell
.\mvnw.cmd verify
```

Run one class:

```powershell
.\mvnw.cmd "-Dtest=TagsIntegrationTests" test
```

Run frontend verification from `frontend`:

```powershell
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

## Removed legacy tests

- A disabled test containing a real email recipient.
- A disabled Redis read with no assertion.
- A database-dependent username lookup and unrelated arithmetic examples.
- A full Spring context test that only verified its own Mockito stub.

These tests produced no confidence and could encourage external side effects.

## Coverage boundary

Phase 1 covers tag normalization and conflicts, journal mapping and transactions,
security, authentication rotation, validation, pagination, combined filters,
controllers, Flyway, and frontend behavior. Real MySQL Testcontainers
compatibility remains explicitly deferred to Phase 3, when container-based test
infrastructure is introduced. The regular Phase 1 suite does not require Docker.
