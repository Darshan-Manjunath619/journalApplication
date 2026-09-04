# Journal Service Setup

Phase 2.2A introduces a second, independently executable Spring Boot application.
It contains no journal business endpoints or database connection yet.

## Build both backend applications

From `journalApplication`:

The repository currently keeps two independent Maven builds so the existing
Spring Boot JAR does not need to be converted into an aggregator project:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd -f journal-service\pom.xml verify
```

The first command builds `journalApplication`, the current Identity/legacy
application. The second builds the new `journal-service` deployable.

## Run the scaffold

```powershell
.\mvnw.cmd -f journal-service\pom.xml spring-boot:run
```

Verify:

```text
GET http://localhost:8081/journal/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

Use `JOURNAL_SERVICE_PORT` to select another port when `8081` is unavailable.
