# Phase 1.2E — Security Verification

## Why

Unit tests prove individual classes. This phase proves that Spring Security,
controllers, JWTs, refresh cookies, services, Flyway, and the database work
together through real HTTP requests.

## Verified flow

```text
Register → Login → Bearer JWT accesses /users/me
→ Refresh cookie rotates → old cookie reused
→ all active refresh sessions become invalid
```

## Verified behavior

| Scenario | Expected result |
|---|---|
| Registration | `USER` role only; password not returned |
| Login | Access JWT JSON plus HttpOnly refresh cookie |
| Protected profile without JWT | `401` Problem Detail |
| USER accesses admin endpoint | `403` Problem Detail |
| ADMIN accesses admin endpoint | `200` |
| Trusted frontend CORS preflight | Allowed |
| Untrusted CORS preflight | `403` |
| Refresh rotation | New cookie replaces old cookie |
| Reuse of old refresh token | Sessions invalidated; `401` |
| Logout | Token revoked and cookie expired |

## Example

After a successful refresh, token A is revoked and token B is issued. If token
A is later presented again, the server treats it as possible theft and revokes
token B as well. The user must log in again.

## Result

The focused full-flow tests passed without requiring production-code changes.
The broader clean-build result is recorded in the Phase 1.2E checkpoint.
