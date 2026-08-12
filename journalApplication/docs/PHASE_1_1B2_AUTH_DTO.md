# Phase 1.1B-2 — User and authentication DTO boundary

## Changes

- Replaced User entity request bodies in signup and login with validated
  request records.
- Added safe registration and token response records.
- Added explicit entity/response mapping so passwords and journal
  relationships cannot be serialized.
- Normalized username and email values before persistence.
- Added duplicate username/email conflict checks.
- Changed registration to assign only the USER role and propagate failures.
- Changed invalid login credentials to a safe 401 ProblemDetail response.

## Contract

Registration accepts username, email, password, and sentiment preference,
then returns safe user data with HTTP 201. Login accepts username and
password, then returns accessToken, tokenType, and expiresIn.

## Security impact

Clients can no longer submit persistence-owned fields such as ID, roles, or
journal relationships during registration. Password values and hashes are
never part of authentication response DTOs.
