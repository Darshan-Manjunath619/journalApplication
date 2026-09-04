package com.darshan.journalApplication.auth;

import java.time.Instant;

public record RotatedRefreshToken(
        AccessTokenIdentity identity,
        String token,
        Instant expiresAt
) {
}
