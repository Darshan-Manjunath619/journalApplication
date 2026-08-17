package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.shared.error.ConflictException;
import com.darshan.journalApplication.auth.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationTests {
    @Mock UserEntryRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock RefreshTokenService refreshTokens;
    UserEntryService service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserEntryService(repository, encoder, refreshTokens);
    }

    @Test void normalizesHashesAndAssignsOnlyUserRole() {
        User input = User.builder().userName(" Alice_1 ").email(" Alice@Example.COM ")
                .password("password123").build();
        when(encoder.encode("password123")).thenReturn("hash");
        when(repository.save(input)).thenReturn(input);
        User saved = service.saveNewUser(input);
        assertEquals("alice_1", saved.getUserName());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals("hash", saved.getPassword());
        assertEquals(List.of("USER"), saved.getRole());
    }

    @Test void rejectsDuplicateUsernameBeforeSaving() {
        User input = User.builder().userName("alice").email("alice@example.com")
                .password("password123").build();
        when(repository.existsByUserName("alice")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.saveNewUser(input));
        verify(repository, never()).save(any());
    }
}
