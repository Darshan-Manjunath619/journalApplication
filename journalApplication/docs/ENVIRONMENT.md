# Environment configuration

The application reads secrets and environment-specific values from
environment variables. The .env.example file is documentation only;
Spring Boot does not load it automatically.

For PowerShell, set DB_PASSWORD and SPRING_PROFILES_ACTIVE in the current
terminal or in the IDE run configuration before starting the application.

Production must provide DB_URL, DB_USERNAME, and DB_PASSWORD through its
secret-management system and use the prod profile. Never commit a populated
.env file.

Credentials previously committed to Git must be rotated. Removing them from
the current files does not remove them from repository history.
