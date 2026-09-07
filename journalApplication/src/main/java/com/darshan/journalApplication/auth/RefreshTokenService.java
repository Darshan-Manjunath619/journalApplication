package com.darshan.journalApplication.auth;

import com.darshan.journalApplication.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final RefreshTokenCodec codec;
    private final Clock clock;
    private final Duration timeToLive;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            RefreshTokenCodec codec,
            Clock clock,
            @Value("${security.refresh-token-ttl:P7D}") Duration timeToLive) {
        this.repository = repository;
        this.codec = codec;
        this.clock = clock;
        this.timeToLive = timeToLive;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        return issueAt(user, clock.instant());
    }

    private IssuedRefreshToken issueAt(User user, Instant createdAt) {
        Instant expiresAt = createdAt.plus(timeToLive);
        String rawToken = codec.generate();
        repository.save(new RefreshToken(
                user, codec.hash(rawToken), expiresAt, createdAt));
        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Transactional(noRollbackFor = {
            InvalidRefreshTokenException.class, RefreshTokenReuseException.class})
    public RotatedRefreshToken rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshToken current = repository
                .findForUpdateByTokenHash(codec.hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (current.getRevokedAt() != null) {
            repository.revokeAllActiveByUserId(current.getUser().getId(), now);
            throw new RefreshTokenReuseException();
        }
        if (!current.getExpiresAt().isAfter(now)) {
            current.revoke(now);
            repository.save(current);
            throw new InvalidRefreshTokenException();
        }

        current.revoke(now);
        repository.save(current);
        IssuedRefreshToken replacement = issueAt(current.getUser(), now);
        return new RotatedRefreshToken(
                identityOf(current.getUser()),
                replacement.token(),
                replacement.expiresAt());
    }

    @Transactional
    public void revokePresented(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findForUpdateByTokenHash(codec.hash(rawToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(this::revoke);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findActive(String rawToken) {
        return repository.findByTokenHash(codec.hash(rawToken))
                .filter(token -> token.isActiveAt(clock.instant()));
    }

    @Transactional
    public void revoke(RefreshToken token) {
        token.revoke(clock.instant());
        repository.save(token);
    }

    @Transactional
    public int revokeAllForUser(Long userId) {
        return repository.revokeAllActiveByUserId(userId, clock.instant());
    }

    private AccessTokenIdentity identityOf(User user) {
        return new AccessTokenIdentity(
                user.getId(), user.getUserName(), user.getRole());
    }
}
