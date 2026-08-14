# Phase 1.2B - Refresh Token Persistence

## Outcome

The authentication module can now generate opaque refresh tokens, persist only
their SHA-256 hashes, find active tokens, and revoke them. No refresh token is
yet sent by an HTTP endpoint; cookie delivery and rotation belong to Phase
1.2C.

## Token flow

1. SecureRandom generates 32 random bytes.
2. The bytes are encoded as a URL-safe token without padding.
3. SHA-256 hashes the raw token into 64 hexadecimal characters.
4. MySQL stores the hash, user, creation time, expiry, and revocation time.
5. The raw token is returned once to the caller for future cookie delivery.
6. A presented token is hashed again before database lookup.

The raw value is never stored in the database or logs.

## Database

Flyway V2 creates refresh_tokens:

- id: primary key
- user_id: required owner, deleted with the user
- token_hash: unique SHA-256 hash
- expires_at: absolute UTC expiry
- revoked_at: null while active
- created_at: UTC creation time

Indexes support owner lookup and expiry cleanup. Flyway moves the database from
version 1 to version 2 before Hibernate validates the entity mapping.

## Access versus refresh token

- Access token: signed JWT, 15 minutes, held in frontend memory, sent as Bearer.
- Refresh token: random opaque value, 7 days, later held in an HttpOnly cookie,
  and used only for refresh/logout.

## Verification

Run: .mvnw.cmd clean test

Result: 43 tests run, 0 failures, 0 errors, and 3 intentionally skipped
external-integration tests.

Tests verify random generation, hash-only persistence, seven-day expiry,
active/expired/revoked lookup, revocation time, Flyway V2, Hibernate validation,
and all existing application behavior.

## Production considerations

Refresh tokens are credentials. Never log raw values. Use HTTPS and Secure
cookies in production. Rotation and reuse detection are required before
exposing refresh over HTTP. Expired rows should later be removed by a bounded
maintenance job.
