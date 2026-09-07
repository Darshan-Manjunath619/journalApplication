package com.darshan.journalservice.identity;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenVerifierTests {
    private static final String SECRET = "test-jwt-secret-that-is-at-least-32-bytes-long";
    private final JwtTokenVerifier verifier =
            new JwtTokenVerifier(SECRET, "journal-application", "journal-spa");

    @Test
    void verifiesIdentityClaims() {
        AccessTokenIdentity identity = verifier.verify(
                token(SECRET, Instant.now().plusSeconds(60), true));

        assertEquals(42L, identity.userId());
        assertEquals("darshan", identity.username());
        assertEquals(List.of("USER"), identity.roles());
    }

    @Test
    void rejectsWrongSignature() {
        String wrongSecret = "different-test-secret-that-is-at-least-32-bytes";
        assertThrows(JwtException.class, () -> verifier.verify(
                token(wrongSecret, Instant.now().plusSeconds(60), true)));
    }

    @Test
    void rejectsExpiredToken() {
        assertThrows(JwtException.class, () -> verifier.verify(
                token(SECRET, Instant.now().minusSeconds(1), true)));
    }

    @Test
    void rejectsMissingUserId() {
        assertThrows(JwtException.class, () -> verifier.verify(
                token(SECRET, Instant.now().plusSeconds(60), false)));
    }

    private String token(String secret, Instant expiration, boolean includeUserId) {
        var builder = Jwts.builder()
                .subject("darshan")
                .claim("roles", List.of("USER"))
                .issuer("journal-application")
                .audience().add("journal-spa").and()
                .issuedAt(Date.from(Instant.now().minusSeconds(1)))
                .expiration(Date.from(expiration));
        if (includeUserId) {
            builder.claim("uid", 42L);
        }
        return builder.signWith(Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
