# Phase 1.13 Integration Verification

## 1.13A Automated baseline

Verified on 2026-08-25:

- `./mvnw.cmd verify`: 93 backend tests passed with no failures, errors, or skips.
- Flyway applied migrations V1 through V5 to the isolated H2 MySQL-mode test database.
- Spring Boot produced `journalApplication-0.0.1-SNAPSHOT.jar`.
- `npm.cmd run lint`: passed.
- `npm.cmd run test -- --run`: 52 frontend tests passed.
- `npm.cmd run build`: TypeScript and Vite production build passed.

The backend test profile uses an in-memory database, disables Redis repositories
and the sentiment scheduler, and uses test-only JWT configuration. The automated
suite does not require developer MySQL, Redis, SMTP, or Weatherstack services.

## 1.13B Real application journey

Pending verification against the local MySQL database and running backend and
frontend:

- Register, login, session refresh, and logout.
- Create, view, search/filter, edit, favorite, and delete a journal.
- Profile update and password change.
- Invalid credentials, validation, protected routes, and API failure responses.
- Flyway schema state, health endpoint, OpenAPI, and browser CORS behavior.
