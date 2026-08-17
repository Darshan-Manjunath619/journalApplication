# Phase 1.2D — Profile and Password Security

## Why this phase exists

The legacy `/user` controller mixed profile behavior with weather, accessed a
repository directly, accepted a JPA entity, and did not safely persist password
changes. The versioned API now keeps HTTP, business, persistence, and response
responsibilities separate.

## Endpoints

```text
GET   /api/v1/users/me
PATCH /api/v1/users/me
PATCH /api/v1/users/me/password
```

The server obtains the username from the validated access JWT. A client cannot
select another user's profile by sending an ID.

## Profile flow

```text
Bearer JWT
  → JwtFilter
  → Authentication username
  → UsersController
  → validated UpdateProfileRequest
  → UserEntryService
  → UserEntryRepository
  → safe UserResponse
```

Only email and the sentiment preference are editable. Usernames remain
immutable because they are currently the JWT subject. Emails are trimmed,
lowercased, and checked for conflicts with another user.

Example request:

```json
{
  "email": "new@example.com",
  "sentimentAnalysis": true
}
```

## Password-change flow

1. Load the user identified by the authenticated JWT subject.
2. Verify `currentPassword` against the stored BCrypt hash.
3. Reject a new password that matches the existing password.
4. BCrypt-encode and save the new password transactionally.
5. Revoke every active refresh token for that user.
6. Existing access JWTs expire naturally within their 15-minute lifetime.

Example request:

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

The API never returns or logs either password or the stored password hash.

## Production considerations

- The service-level email conflict check provides a clear API error, but a
  database unique constraint will also be added with the planned schema/index
  improvements to close concurrent-update races.
- Immediate access-token invalidation would require a token blacklist or user
  security-version check on every request. The current design avoids that state
  and limits remaining access to the short JWT lifetime.
- The legacy `/user` route remains temporarily for compatibility and will be
  removed after versioned API/frontend parity.

## Interview explanation

I made profile operations identity-based: the server reads the authenticated
JWT subject instead of accepting a user ID. Profile changes use validated DTOs,
normalization, conflict checks, transactions, and safe response mapping. A
password change verifies the existing BCrypt hash, stores a new hash, and
revokes all refresh sessions, limiting any existing access JWT to its short
remaining lifetime.
