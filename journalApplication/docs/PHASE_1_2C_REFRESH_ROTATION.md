# Phase 1.2C — Refresh Rotation and Logout

## Why this phase exists

An access JWT is intentionally short-lived. Without a refresh mechanism, users
would need to sign in every 15 minutes. Making the access JWT long-lived would
increase the damage if it were stolen. This phase adds a longer browser session
without placing a long-lived credential in JavaScript-accessible storage.

## Token locations

| Credential | Lifetime | Browser location | Server storage |
|---|---:|---|---|
| Access JWT | 15 minutes | Frontend memory | Not stored |
| Refresh token | 7 days | HttpOnly, SameSite=Lax cookie | SHA-256 hash only |

The frontend sends the access token in `Authorization: Bearer <token>`. The
browser automatically sends the refresh cookie only to the configured auth
path. JavaScript cannot read an HttpOnly cookie.

## Request flow

### Login

1. `POST /api/v1/auth/login` authenticates the normalized username/password.
2. The server creates an access JWT and a random opaque refresh token.
3. Only the refresh-token hash is inserted into `refresh_tokens`.
4. JSON contains the access token; `Set-Cookie` contains the raw refresh token.

### Refresh rotation

1. `POST /api/v1/auth/refresh` receives the cookie automatically.
2. A presented browser `Origin` must match an allowed frontend origin.
3. The server hashes the presented token and locks its database row.
4. The old token is revoked and a new token is issued in one transaction.
5. The response replaces the cookie and returns a new access JWT.

The database lock prevents two concurrent requests from successfully rotating
the same one-time token.

### Reuse detection

If an already-revoked refresh token is presented, the server treats it as
possible token theft and revokes every active refresh token for that user. The
client receives `401` and must sign in again.

### Logout

`POST /api/v1/auth/logout` revokes the presented database token and returns an
expired refresh cookie. Access JWTs remain valid only until their short expiry;
the server does not maintain an access-token blacklist.

## Configuration

Local development uses `/journal/api/v1/auth` because the local server has the
`/journal` context path. Production defaults to `/api/v1/auth` and secure
cookies. Configure:

```text
REFRESH_TOKEN_TTL=P7D
REFRESH_COOKIE_NAME=refresh_token
REFRESH_COOKIE_PATH=/journal/api/v1/auth
REFRESH_COOKIE_SECURE=false
```

Production must use HTTPS and `REFRESH_COOKIE_SECURE=true`. Never store a raw
refresh token or access JWT in logs or the database.

## Interview explanation

I used short-lived stateless access JWTs plus rotating opaque refresh tokens.
The refresh token is HttpOnly in the browser and hashed in MySQL. Every refresh
revokes the old token under a row lock and issues a replacement. Reuse of a
revoked token invalidates the user's remaining sessions, providing a practical
response to refresh-token theft while keeping access-token checks stateless.
