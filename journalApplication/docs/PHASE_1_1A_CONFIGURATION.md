# Phase 1.1A — Configuration and secret safety

## Changes

- Replaced tracked database, mail, weather, and MongoDB credentials with
  environment-based configuration.
- Added local, dev, and production profile responsibilities.
- Added an ignored local .env import and a safe .env.example template.
- Corrected MySQL Connector coordinates.
- Disabled Redis repository scanning.
- Disabled the incomplete sentiment scheduler and email service by default.
- Prevented weather and Redis calls when no weather API key is configured.
- Disabled open-in-view and application TRACE logging.

## Verification

The clean test suite passed: nine tests, zero failures, zero errors, and
three intentionally skipped legacy tests. The local profile loaded the
ignored .env file and connected successfully to MySQL.

## Production notes

Previously committed credentials must be rotated because Git history still
contains them. Production must provide database secrets externally and use
the prod profile. The local allowPublicKeyRetrieval setting must not replace
TLS-secured production database connections.
