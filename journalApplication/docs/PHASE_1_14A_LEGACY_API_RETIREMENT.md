# Phase 1.14A — Legacy API Retirement

## Why

The application had two HTTP contracts for the same features: old unversioned
routes such as `/public/**` and the supported `/api/v1/**` routes. Keeping both
would duplicate maintenance and leave older security behavior reachable.

Example: registration must now use `POST /api/v1/auth/register`; the former
`POST /public/signup` controller no longer exists.

## What changed

- Removed the legacy public, user, and journal controllers and their obsolete tests.
- Moved the admin user list to `GET /api/v1/admin/users`.
- Kept the admin response behind `ROLE_ADMIN` and mapped users to safe DTOs.
- Removed legacy route rules from Spring Security; unmatched routes are denied.
- Updated security and OpenAPI tests to verify the supported contract.

## Architecture impact

All browser and API clients now depend on one versioned HTTP boundary:

```text
Client -> /api/v1 controllers -> application services -> repositories -> database
```

The servlet context remains `/journal`, so the complete local URL starts with
`http://localhost:8080/journal/api/v1`.

## Verification

- Focused security and OpenAPI tests verify the admin role rule, authentication
  failures, CORS behavior, legacy-route denial, and generated API contract.
- The complete backend suite must pass before this sub-phase is committed.
