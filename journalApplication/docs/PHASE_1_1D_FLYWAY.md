# Phase 1.1D - Flyway Database Migration Foundation

## Why Flyway

Hibernate now validates the schema instead of changing it. Flyway owns schema
creation and future upgrades through ordered, version-controlled SQL files.
This makes database changes repeatable, reviewable, and visible in
flyway_schema_history.

## Startup flow

For a fresh database:

1. Spring connects to the configured database.
2. Flyway creates flyway_schema_history.
3. Flyway applies V1__create_legacy_schema.sql.
4. Flyway records version 1 and its checksum.
5. Hibernate validates that the JPA mappings match the migrated schema.
6. The application starts.

For later changes, add V2, V3, and subsequent migrations. Never edit a
migration already applied to a shared environment because its stored checksum
will no longer match.

## Existing local MySQL database

The existing schema was previously created by Hibernate and is non-empty.
Flyway must be told that it already represents version 1.

Before the first Flyway-enabled startup:

1. Stop the application.
2. Back up the JournalApplication database with MySQL Workbench or mysqldump.
3. Confirm the existing tables match users, journal_entries, and user_roles.
4. Set FLYWAY_BASELINE_ON_MIGRATE=true for one startup only.
5. Start the application and confirm Flyway creates a baseline row at version 1.
6. Remove the variable or set it back to false.

PowerShell one-time startup:

    $env:FLYWAY_BASELINE_ON_MIGRATE = "true"
    .mvnw.cmd spring-boot:run

After successful startup, stop the application and clear the temporary value:

    Remove-Item Env:FLYWAY_BASELINE_ON_MIGRATE

Do not use baseline-on-migrate as a permanent production default. It can make
an unexpected unmanaged schema appear legitimate.

## Fresh database

For an empty database, leave FLYWAY_BASELINE_ON_MIGRATE unset or false. Flyway
will execute V1 and create all legacy tables.

## Rollback model

Versioned migrations are forward-only in this project. If a production
migration fails, restore the verified database backup when necessary, correct
the migration before wider deployment, or add a new corrective migration.
Never manually delete Flyway history rows to hide a failed change.

## Verification

Run: .mvnw.cmd clean test

Result: 30 tests run, 0 failures, 0 errors, and 3 intentionally skipped
external-integration tests.

The focused migration test confirms version 1 is current and verifies creation
of users, journal_entries, and user_roles in the application schema.

## Production considerations

H2 verifies migration wiring and basic SQL compatibility, but it is not a full
MySQL replacement. Later database integration testing should run migrations
against an isolated MySQL instance, preferably through Testcontainers. Database
backups and restore drills remain required before production migrations.
