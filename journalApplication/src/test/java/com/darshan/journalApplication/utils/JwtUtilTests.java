package com.darshan.journalApplication.utils;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {
    private static final String SECRET =
            "test-only-jwt-secret-at-least-32-bytes-long";

    @Test
    void generatesAndValidatesConfiguredAccessToken() {
        JwtUtil jwt = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofMinutes(15));

        String token = jwt.generateToken("darshan");

        assertEquals("darshan", jwt.extractUsername(token));
        assertTrue(jwt.validateToken(token));
        assertEquals(900, jwt.getAccessTokenExpiresInSeconds());
    }

    @Test
    void rejectsTokenIssuedForDifferentAudience() {
        JwtUtil issuer = new JwtUtil(
                SECRET, "journal-test", "other-client", Duration.ofMinutes(15));
        JwtUtil verifier = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofMinutes(15));

        String token = issuer.generateToken("darshan");

        assertThrows(JwtException.class, () -> verifier.extractUsername(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtUtil jwt = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofSeconds(-1));

        String token = jwt.generateToken("darshan");

        assertThrows(JwtException.class, () -> jwt.extractUsername(token));
    }

    @Test
    void rejectsShortSigningSecret() {
        assertThrows(IllegalArgumentException.class, () ->
                new JwtUtil("too-short", "issuer", "audience", Duration.ofMinutes(15)));
    }
}
