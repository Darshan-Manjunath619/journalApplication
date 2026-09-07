package com.darshan.journalApplication.utils;

import com.darshan.journalApplication.auth.AccessTokenIdentity;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTests {
    private static final String SECRET =
            "test-only-jwt-secret-at-least-32-bytes-long";

    @Test
    void generatesAndValidatesConfiguredAccessToken() {
        JwtUtil jwt = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofMinutes(15));

        String token = jwt.generateToken(identity());

        assertEquals("darshan", jwt.extractUsername(token));
        assertEquals(42L, jwt.extractIdentity(token).userId());
        assertEquals(List.of("USER"), jwt.extractIdentity(token).roles());
        assertTrue(jwt.validateToken(token));
        assertEquals(900, jwt.getAccessTokenExpiresInSeconds());
    }

    @Test
    void rejectsTokenIssuedForDifferentAudience() {
        JwtUtil issuer = new JwtUtil(
                SECRET, "journal-test", "other-client", Duration.ofMinutes(15));
        JwtUtil verifier = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofMinutes(15));

        String token = issuer.generateToken(identity());

        assertThrows(JwtException.class, () -> verifier.extractUsername(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtUtil jwt = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofSeconds(-1));

        String token = jwt.generateToken(identity());

        assertThrows(JwtException.class, () -> jwt.extractUsername(token));
    }

    @Test
    void rejectsShortSigningSecret() {
        assertThrows(IllegalArgumentException.class, () ->
                new JwtUtil("too-short", "issuer", "audience", Duration.ofMinutes(15)));
    }

    @Test
    void rejectsTokenWhenSignedIdentityIsTamperedWith() {
        JwtUtil jwt = new JwtUtil(
                SECRET, "journal-test", "journal-spa-test", Duration.ofMinutes(15));
        String token = jwt.generateToken(identity());
        String[] parts = token.split("\\.");
        char replacement = parts[1].charAt(parts[1].length() - 1) == 'A' ? 'B' : 'A';
        parts[1] = parts[1].substring(0, parts[1].length() - 1) + replacement;
        String tampered = String.join(".", parts);

        assertThrows(JwtException.class, () -> jwt.extractIdentity(tampered));
    }

    private AccessTokenIdentity identity() {
        return new AccessTokenIdentity(42L, "darshan", List.of("USER"));
    }
}
