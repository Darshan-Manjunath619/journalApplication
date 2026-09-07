# Journal data migration tool

This one-time CLI copies journals, tags, and links from the legacy Identity
database into the empty Journal Service database. IDs are preserved and legacy
user_id values become owner_id values.

Run from this directory. Preview is the safe default:

    ..\mvnw.cmd test
    ..\mvnw.cmd exec:java

After backing up both databases and reviewing the preview, execute with:

    ..\mvnw.cmd exec:java -Dexec.args="--execute"

Configuration comes from ../.env by default. Environment variables override the
file. MIGRATION_ENV_FILE may point to another ignored properties file. The tool
uses DB_URL/DB_USERNAME/DB_PASSWORD for the source and JOURNAL_DB_URL,
JOURNAL_DB_USERNAME/JOURNAL_DB_PASSWORD for the target. Credentials are not
printed. The target journal tables must be empty.
