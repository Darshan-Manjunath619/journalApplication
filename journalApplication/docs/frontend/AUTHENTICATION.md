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

## Protected requests and refresh retry

Dashboard and profile routes render only for an authenticated session. A `401`
from a protected API request starts one shared refresh request. When refresh
succeeds, the client stores the new access token and retries each original
request once. A rejected refresh clears the session and returns the user to
login; auth endpoints never trigger refresh, preventing retry loops.

## Logout

Logout posts to `/api/v1/auth/logout`, allowing the backend to revoke the
refresh token and clear its cookie. The frontend clears its in-memory access
token and user state even if the network request fails, then returns to login.

## Profile management

The profile screen displays username and roles as read-only identity fields.
Only email and sentiment preference are sent to `PATCH /api/v1/users/me`.
The returned profile replaces the shared user in `AuthContext`, keeping the
shell and future features consistent without another page load.

Password changes require the current password and matching new-password
confirmation. After `PATCH /api/v1/users/me/password` succeeds, the frontend
clears its session and returns to login because the backend revokes every
refresh token belonging to the user.

Phase 1.9A through 1.9D complete the frontend authentication and profile flow.
