package com.darshan.journalservice.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtTokenVerifier {
    private static final String USER_ID_CLAIM = "uid";
    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;
    private final String issuer;
    private final String audience;

    public JwtTokenVerifier(@Value("${security.jwt.secret}") String secret,
                            @Value("${security.jwt.issuer}") String issuer,
                            @Value("${security.jwt.audience}") String audience) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 UTF-8 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    public AccessTokenIdentity verify(String token) {
        Claims claims = Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rawUserId = claims.get(USER_ID_CLAIM);
        Object rawRoles = claims.get(ROLES_CLAIM);
        if (!(rawUserId instanceof Number userId)) {
            throw new JwtException("Access token is missing a valid uid claim");
        }
        if (!(rawRoles instanceof List<?> roleValues)
                || roleValues.stream().anyMatch(role -> !(role instanceof String))) {
            throw new JwtException("Access token is missing a valid roles claim");
        }

        try {
            return new AccessTokenIdentity(userId.longValue(), claims.getSubject(),
                    roleValues.stream().map(String.class::cast).toList());
        } catch (IllegalArgumentException exception) {
            throw new JwtException("Access token identity claims are invalid", exception);
        }
    }
}
