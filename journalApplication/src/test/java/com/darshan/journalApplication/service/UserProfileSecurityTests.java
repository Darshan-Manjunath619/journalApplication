package com.darshan.journalApplication.service;

import com.darshan.journalApplication.auth.RefreshTokenService;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.shared.error.ConflictException;
import com.darshan.journalApplication.user.InvalidCurrentPasswordException;
import com.darshan.journalApplication.user.dto.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileSecurityTests {
    private UserEntryRepository repository;
    private PasswordEncoder encoder;
    private RefreshTokenService refreshTokens;
    private UserEntryService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserEntryRepository.class);
        encoder = mock(PasswordEncoder.class);
        refreshTokens = mock(RefreshTokenService.class);
        service = new UserEntryService(repository, encoder, refreshTokens);
    }

    @Test
    void returnsProfileForAuthenticatedUsername() {
        User user = user();
        when(repository.findByUserName("alice")).thenReturn(user);

        assertSame(user, service.getProfile("alice"));
        verify(repository).findByUserName("alice");
    }

    @Test
    void normalizesAndUpdatesAllowedProfileFields() {
        User user = user();
        when(repository.findByUserName("alice")).thenReturn(user);
        when(repository.save(user)).thenReturn(user);

        User updated = service.updateProfile("alice",
                new UpdateProfileRequest(" New@Example.COM ", true));

        assertEquals("new@example.com", updated.getEmail());
        assertTrue(updated.isSentimentAnalysis());
        verify(repository).existsByEmailAndIdNot("new@example.com", 10L);
        verify(repository).save(user);
    }

    @Test
    void rejectsAnotherUsersEmail() {
        User user = user();
        when(repository.findByUserName("alice")).thenReturn(user);
        when(repository.existsByEmailAndIdNot("used@example.com", 10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.updateProfile(
                "alice", new UpdateProfileRequest("used@example.com", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsWrongCurrentPasswordWithoutChangingSessions() {
        User user = user();
        when(repository.findByUserName("alice")).thenReturn(user);
        when(encoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCurrentPasswordException.class,
                () -> service.changePassword("alice", "wrong-password", "new-password"));

        assertEquals("stored-hash", user.getPassword());
        verify(repository, never()).save(any());
        verifyNoInteractions(refreshTokens);
    }

    @Test
    void hashesNewPasswordAndRevokesAllRefreshSessions() {
        User user = user();
        when(repository.findByUserName("alice")).thenReturn(user);
        when(encoder.matches("old-password", "stored-hash")).thenReturn(true);
        when(encoder.matches("new-password", "stored-hash")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword("alice", "old-password", "new-password");

        assertEquals("new-hash", user.getPassword());
        verify(repository).save(user);
        verify(refreshTokens).revokeAllForUser(10L);
    }

    private User user() {
        return User.builder().id(10L).userName("alice")
                .email("alice@example.com").password("stored-hash").build();
    }
}
