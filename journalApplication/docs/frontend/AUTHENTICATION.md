# Frontend Authentication

## Security boundary

- The JWT access token is held only in JavaScript memory.
- The rotating refresh token is an HttpOnly cookie managed by the browser.
- React cannot read the refresh-token cookie.
- Protected requests use `Authorization: Bearer <access-token>`.
- Frontend validation improves feedback; Spring Boot remains trusted.

## Session bootstrap

```text
Page load
  -> POST /api/v1/auth/refresh (browser includes cookie)
  -> store returned access token in memory
  -> GET /api/v1/users/me with Bearer token
  -> publish authenticated user through AuthContext
```

A refresh `401` produces `unauthenticated`. Network or server failures produce
`error`, allowing retry instead of incorrectly treating an outage as logout.

## Authentication states

| Status | Meaning |
|---|---|
| `loading` | Session restoration is running |
| `authenticated` | Access token and current profile are available |
| `unauthenticated` | No valid refresh session exists |
| `error` | Session restoration could not be completed |

The authentication provider is outside React development `StrictMode` because
refresh rotates server state and must not be replayed by development checks.

## Login and registration

Login validates input, posts credentials to `/api/v1/auth/login`, stores the
returned access token in memory, loads `/api/v1/users/me`, and then navigates
to the dashboard. Registration posts to `/api/v1/auth/register` and redirects
to login with a confirmation message; it does not automatically authenticate
because registration does not return tokens.

Phase 1.9A and 1.9B implement state, bootstrap, login, and registration.
Protected routes, automatic `401` retry, logout, and profile editing follow.
