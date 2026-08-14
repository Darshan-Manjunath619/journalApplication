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
        Instant createdAt = clock.instant();
        Instant expiresAt = createdAt.plus(timeToLive);
        String rawToken = codec.generate();
        repository.save(new RefreshToken(
                user, codec.hash(rawToken), expiresAt, createdAt));
        return new IssuedRefreshToken(rawToken, expiresAt);
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
}
