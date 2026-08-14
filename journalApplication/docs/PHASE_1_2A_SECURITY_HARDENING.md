# Phase 1.2A - Access Token and HTTP Security Hardening

## Outcome

Access JWTs are now short-lived, environment-configured, and validated for
signature, expiry, issuer, and audience. Unknown endpoints are denied by
default. Authentication and authorization failures use JSON Problem Detail
responses containing the request correlation ID.

## Request flow

1. Login verifies the username and BCrypt password.
2. JwtUtil creates a 15-minute token with subject, issuer, audience, issued
   time, expiration, and a unique token ID.
3. The client sends the token in the Authorization Bearer header.
4. JwtFilter validates the token and loads the user into SecurityContext.
5. Spring Security applies endpoint and role rules.
6. Missing or invalid authentication returns 401; insufficient role returns
   403.

## Route policy

- Public: /public/** and Swagger/OpenAPI routes.
- Authenticated: /journal/** and /user/**.
- ADMIN role: /admin/**.
- Everything else: denied.

## Environment

JWT_SECRET is required and must contain at least 32 UTF-8 bytes. It must be a
random secret stored in the ignored local .env file or a production secret
manager. JWT issuer, audience, lifetime, and allowed frontend origins are also
environment-configurable.

## Verification

Run: .mvnw.cmd clean test

Result: 39 tests run, 0 failures, 0 errors, and 3 intentionally skipped
external-integration tests.

Tests cover token validation, wrong audience, expiry, short keys, public access,
missing and malformed tokens, USER versus ADMIN access, CORS, registration
roles, and the login response contract.

## Deferred

Refresh-token persistence, rotation, reuse detection, HttpOnly cookies, logout,
and cookie-origin protection are implemented in the following security
increments.
