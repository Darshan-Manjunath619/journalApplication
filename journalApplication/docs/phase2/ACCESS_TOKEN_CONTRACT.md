# Phase 2 Access-Token Identity Contract

## Purpose

Resource services must identify the authenticated owner without querying the
Identity database. Identity Service therefore signs these claims:

```json
{
  "sub": "darshan",
  "uid": 42,
  "roles": ["USER"],
  "iss": "journal-application",
  "aud": ["journal-spa"],
  "iat": 1788400000,
  "exp": 1788400900,
  "jti": "unique-token-id"
}
```

- `uid` is the immutable database user ID and becomes journal `owner_id`.
- `sub` is the username used by the Phase 1 monolith.
- `roles` carries authorization roles such as `USER` or `ADMIN`.

The browser cannot supply or override ownership in a request body. A resource
service trusts these values only after verifying the signature, issuer, audience,
and expiry.

## Login and refresh consistency

Both login and refresh create `AccessTokenIdentity` from the persisted user.
This prevents a refreshed token from losing `uid` or role claims.

The Phase 1 filter still reloads current user details from MySQL so existing
authorization behavior remains unchanged. When Journal Service is extracted, its
resource-server filter will build authentication directly from verified claims
and will not access the Identity database.

## Migration security

Phase 2.1A continues using the existing HMAC signature for compatibility.
Phase 2.5 replaces shared signing secrets with an Identity-owned private key and
verification-only public keys in resource services.

Tokens issued before Phase 2.1A do not contain the required claims and are
rejected with `401`. Existing valid refresh cookies can obtain the new format,
and the old access-token lifetime is limited to 15 minutes.
