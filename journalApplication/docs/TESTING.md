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

Phase 1.6B adds missing focused unit/controller tests for tag rules, mappers,
combined journal updates, and validation responses. Phase 1.6C strengthens
repository, transaction, and combined-filter verification and evaluates real
MySQL Testcontainers separately from application containerization.
