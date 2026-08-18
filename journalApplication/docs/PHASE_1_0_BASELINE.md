# Phase 1.0 — Repository Baseline

## Architecture

The project is a Java 17, Spring Boot 3.5.4 layered monolith. HTTP requests
pass through Spring Security and a JWT filter, then controllers, services,
Spring Data repositories, Hibernate/JPA, and MySQL. Redis/weather, SMTP,
and a weekly sentiment scheduler are additional integrations.

Packages are grouped by technical layer. Controllers serialize persistence
entities, and domain boundaries are not enforced.

## Existing behavior

- Signup and username/password login with BCrypt and JWT.
- User-owned journal CRUD and an ADMIN endpoint.
- Weather greeting cached through Redis.
- Email support and an incomplete scheduled sentiment workflow.
- Swagger/OpenAPI.

## Baseline verification

Command: mvnw.cmd test

Initial result: FAILED during test compilation because a test imported
org.bson.assertions.Assertions without a BSON dependency.

Final command: mvnw.cmd clean test

Final result: BUILD SUCCESS. Nine tests ran with zero failures and zero
errors; three unsafe legacy tests were skipped. Tests use an in-memory H2
database and do not contact SMTP, Redis, Weatherstack, or developer MySQL.

Warnings include relocated MySQL coordinates, Lombok builder defaults,
a deprecated security API, and unchecked Redis types.

## Priority risks

1. Registration grants ADMIN to every user.
2. Database, SMTP, weather, MongoDB, and JWT secrets are tracked.
3. HTTP APIs accept and expose JPA entities, including sensitive fields.
4. Security permits unspecified future routes by default.
5. Password update is neither persisted nor safely hashed.
6. Tests depend on developer infrastructure and include a live email call.
7. Hibernate update owns schema changes; Flyway is disabled.
8. Journal ownership checks load full collections and duplicate queries.

## Baseline conclusion

The main sources compile and the isolated baseline suite is green.
Phase 1.1 can now improve configuration, validation, DTO boundaries, and
error handling from a reproducible starting point. Existing credentials
must be considered compromised and rotated outside Git.
