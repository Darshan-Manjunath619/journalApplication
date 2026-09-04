# Journal Service Setup

Phase 2.2 introduces a second, independently executable Spring Boot application.
The service now owns its journal/tag persistence model, while business HTTP
endpoints are introduced incrementally.

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

Create the empty service-owned MySQL database once:

```sql
CREATE DATABASE JournalService;
```

Add these values to the ignored root `.env` file:

```properties
JOURNAL_DB_URL=jdbc:mysql://localhost:3306/JournalService?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
JOURNAL_DB_USERNAME=root
JOURNAL_DB_PASSWORD=your-local-password
JOURNAL_SERVICE_PORT=8081
```

Then run:

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

On first startup, Flyway creates `journal_entries`, `tags`,
`journal_entry_tags`, and `flyway_schema_history`. It does not create or query
an Identity-owned `users` table.
