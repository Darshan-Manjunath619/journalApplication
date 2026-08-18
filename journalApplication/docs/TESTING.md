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

Run everything:

```powershell
.\mvnw.cmd clean test
```

Run one class:

```powershell
.\mvnw.cmd "-Dtest=TagsIntegrationTests" test
```

`mvn verify` is the final Phase 1.6 acceptance command.

## Removed legacy tests

- A disabled test containing a real email recipient.
- A disabled Redis read with no assertion.
- A database-dependent username lookup and unrelated arithmetic examples.
- A full Spring context test that only verified its own Mockito stub.

These tests produced no confidence and could encourage external side effects.

## Remaining planned coverage

Phase 1.6B added focused unit/controller tests for tag normalization, duplicate
and deletion rules, journal mapping and combined updates, tag clearing, date
validation, and safe query/body validation responses. Phase 1.6C-1 strengthened
repository, transaction, and combined-filter verification. Phase 1.6C-2 will
run the same persistence boundary against a temporary real MySQL database with
Testcontainers. It remains pending until a Docker engine is available; the
regular test suite does not require Docker.
