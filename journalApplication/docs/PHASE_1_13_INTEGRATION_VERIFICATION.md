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

Verified against local MySQL and the running backend/frontend on 2026-08-25 and
2026-08-26:

- Health was `UP`; OpenAPI, frontend, and trusted-origin CORS returned `200`.
- Flyway validated the real MySQL schema at version 5 with no pending migration.
- Invalid login returned `401`; invalid registration returned `400`.
- Registration assigned only `USER`; login and owned profile access succeeded.
- Tag and journal create, detail, combined search/filter, update, favorite, and
  delete succeeded. The disposable journal and tag were removed afterward.
- Profile update, refresh-token rotation, logout, and unauthenticated `401`
  behavior succeeded.
- Password change returned `204`; the old password returned `401`, and the new
  password authenticated successfully.
- The user manually confirmed the React journey, browser refresh/session
  restoration, protected-route redirect after logout, and absence of CORS errors.

The disposable account remains clearly named `phase113b_20260825102210`; it has
no journal or tag records and can be removed manually when no longer useful.
