package com.darshan.journalApplication.auth;

import java.time.Instant;

public record RotatedRefreshToken(
        String userName,
        String token,
        Instant expiresAt
) {
}
