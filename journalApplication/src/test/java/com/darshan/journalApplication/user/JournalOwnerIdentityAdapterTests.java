package com.darshan.journalApplication.user;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.UserEntryRepository;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JournalOwnerIdentityAdapterTests {
    private final UserEntryRepository users = mock(UserEntryRepository.class);
    private final JournalOwnerIdentityAdapter adapter = new JournalOwnerIdentityAdapter(users);

    @Test
    void returnsOnlyTheStableUserId() {
        when(users.findByUserName("alice"))
                .thenReturn(User.builder().id(42L).userName("alice").build());
        assertEquals(42L, adapter.requireOwnerId("alice"));
    }

    @Test
    void missingUserIsRejected() {
        assertThrows(ResourceNotFoundException.class,
                () -> adapter.requireOwnerId("missing"));
    }
}
