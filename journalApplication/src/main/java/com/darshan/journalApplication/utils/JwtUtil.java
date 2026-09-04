package com.darshan.journalApplication.utils;

import com.darshan.journalApplication.auth.AccessTokenIdentity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final String USER_ID_CLAIM = "uid";
    private static final String ROLES_CLAIM = "roles";

    private final String secret;
    private final String issuer;
    private final String audience;
    private final Duration accessTokenTtl;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer:journal-application}") String issuer,
            @Value("${security.jwt.audience:journal-spa}") String audience,
            @Value("${security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 UTF-8 bytes");
        }
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
        this.accessTokenTtl = accessTokenTtl;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractIdentity(token).username();
    }

    public AccessTokenIdentity extractIdentity(String token) {
        Claims claims = extractAllClaims(token);
        Object rawUserId = claims.get(USER_ID_CLAIM);
        Object rawRoles = claims.get(ROLES_CLAIM);

        if (!(rawUserId instanceof Number userId)) {
            throw new JwtException("Access token is missing a valid uid claim");
        }
        if (!(rawRoles instanceof List<?> roleValues)
                || roleValues.stream().anyMatch(role -> !(role instanceof String))) {
            throw new JwtException("Access token is missing a valid roles claim");
        }

        List<String> roles = roleValues.stream()
                .map(String.class::cast)
                .toList();
        try {
            return new AccessTokenIdentity(
                    userId.longValue(), claims.getSubject(), roles);
        } catch (IllegalArgumentException exception) {
            throw new JwtException("Access token identity claims are invalid", exception);
        }
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(AccessTokenIdentity identity) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(identity.username())
                .claim(USER_ID_CLAIM, identity.userId())
                .claim(ROLES_CLAIM, identity.roles())
                .issuer(issuer)
                .audience().add(audience).and()
                .id(UUID.randomUUID().toString())
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(issuedAt)
                .expiration(Date.from(issuedAt.toInstant().plus(accessTokenTtl)))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenTtl.toSeconds();
    }

}
