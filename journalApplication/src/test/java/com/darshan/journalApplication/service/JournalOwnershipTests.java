package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.*;
import com.darshan.journalApplication.journal.dto.UpdateJournalRequest;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JournalOwnershipTests {
    @Mock JournalEntryRepository repository;
    @Mock UserEntryService users;
    JournalEntryService service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new JournalEntryService(repository, users);
    }

    @Test void missingOrUnownedEntryIsNotFound() {
        when(repository.findByIdAndUserUserName(9L, "alice")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getOwned(9L, "alice"));
    }

    @Test void updateUsesOwnedQueryAndChangesOnlyProvidedFields() {
        JournalEntry entry = JournalEntry.builder().id(2L).title("Old").content("Keep").build();
        when(repository.findByIdAndUserUserName(2L, "alice")).thenReturn(Optional.of(entry));
        when(repository.save(entry)).thenReturn(entry);
        JournalEntry result = service.updateOwned(2L, "alice",
                new UpdateJournalRequest(" New ", null, null));
        assertEquals("New", result.getTitle());
        assertEquals("Keep", result.getContent());
        verify(repository).findByIdAndUserUserName(2L, "alice");
    }
}
