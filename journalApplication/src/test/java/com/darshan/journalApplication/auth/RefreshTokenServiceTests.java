package com.darshan.journalApplication.auth;

import com.darshan.journalApplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTests {
    private static final Instant NOW = Instant.parse("2026-08-14T00:00:00Z");

    private RefreshTokenRepository repository;
    private RefreshTokenCodec codec;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        codec = new RefreshTokenCodec();
        service = new RefreshTokenService(
                repository, codec, Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofDays(7));
    }

    @Test
    void issuesRandomTokenButStoresOnlyItsHash() {
        User user = User.builder().id(10L).userName("darshan").build();

        IssuedRefreshToken issued = service.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        RefreshToken stored = captor.getValue();

        assertNotNull(issued.token());
        assertFalse(issued.token().isBlank());
        assertNotEquals(issued.token(), stored.getTokenHash());
        assertEquals(64, stored.getTokenHash().length());
        assertEquals(codec.hash(issued.token()), stored.getTokenHash());
        assertEquals(NOW.plus(Duration.ofDays(7)), issued.expiresAt());
        assertEquals(NOW, stored.getCreatedAt());
    }

    @Test
    void findsOnlyUnexpiredAndUnrevokedTokens() {
        User user = User.builder().id(10L).build();
        RefreshToken active = new RefreshToken(
                user, codec.hash("active"), NOW.plusSeconds(60), NOW);
        RefreshToken expired = new RefreshToken(
                user, codec.hash("expired"), NOW.minusSeconds(1), NOW.minusSeconds(60));
        RefreshToken revoked = new RefreshToken(
                user, codec.hash("revoked"), NOW.plusSeconds(60), NOW);
        revoked.revoke(NOW.minusSeconds(1));

        when(repository.findByTokenHash(codec.hash("active"))).thenReturn(Optional.of(active));
        when(repository.findByTokenHash(codec.hash("expired"))).thenReturn(Optional.of(expired));
        when(repository.findByTokenHash(codec.hash("revoked"))).thenReturn(Optional.of(revoked));

        assertTrue(service.findActive("active").isPresent());
        assertTrue(service.findActive("expired").isEmpty());
        assertTrue(service.findActive("revoked").isEmpty());
        assertTrue(service.findActive("unknown").isEmpty());
    }

    @Test
    void revokesTokenAtCurrentUtcInstant() {
        RefreshToken token = new RefreshToken(
                User.builder().id(10L).build(), codec.hash("token"),
                NOW.plusSeconds(60), NOW);

        service.revoke(token);

        assertEquals(NOW, token.getRevokedAt());
        verify(repository).save(token);
    }

    @Test
    void generatedTokensAreNotRepeated() {
        assertNotEquals(codec.generate(), codec.generate());
    }
}
