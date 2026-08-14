package com.darshan.journalApplication.auth;

import java.time.Instant;

public record IssuedRefreshToken(String token, Instant expiresAt) {
}
