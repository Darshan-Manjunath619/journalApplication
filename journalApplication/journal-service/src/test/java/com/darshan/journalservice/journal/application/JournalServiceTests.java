package com.darshan.journalservice.journal.application;

import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.journal.persistence.JournalEntryRepository;
import com.darshan.journalservice.shared.error.InvalidQueryParameterException;
import com.darshan.journalservice.shared.error.ResourceNotFoundException;
import com.darshan.journalservice.tag.application.TagService;
import com.darshan.journalservice.tag.domain.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JournalServiceTests {
    private final JournalEntryRepository journals = mock(JournalEntryRepository.class);
    private final TagService tags = mock(TagService.class);
    private JournalService service;

    @BeforeEach
    void setUp() {
        service = new JournalService(journals, tags);
    }

    @Test
    void createAssignsAuthenticatedOwnerAndOwnedTags() {
        JournalEntry entry = JournalEntry.builder().title("Learning").build();
        Set<Tag> resolved = new LinkedHashSet<>();
        when(tags.resolveOwned(Set.of(7L), 42L)).thenReturn(resolved);
        when(journals.save(entry)).thenReturn(entry);

        JournalEntry result = service.create(entry, 42L, Set.of(7L));

        assertEquals(42L, result.getOwnerId());
        assertNotNull(result.getDate());
        assertSame(resolved, result.getTags());
    }

    @Test
    void missingOrOtherOwnersJournalIsNotFound() {
        when(journals.findByIdAndOwnerId(9L, 42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.getOwned(9L, 42L));
    }

    @Test
    void updateChangesOnlyProvidedFields() {
        JournalEntry entry = JournalEntry.builder().id(2L).ownerId(42L)
                .title("Old").content("Keep").build();
        when(journals.findByIdAndOwnerId(2L, 42L)).thenReturn(Optional.of(entry));
        when(journals.save(entry)).thenReturn(entry);

        JournalEntry result = service.update(2L, 42L,
                new UpdateJournalCommand(" New ", null, true, null));

        assertEquals("New", result.getTitle());
        assertEquals("Keep", result.getContent());
        assertTrue(result.isFavorite());
    }

    @Test
    void reversedDateRangeIsRejectedBeforeQuery() {
        JournalSearchCriteria criteria = new JournalSearchCriteria(null,
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"), null, null);

        assertThrows(InvalidQueryParameterException.class, () ->
                service.searchOwned(42L, criteria, 0, 20,
                        "createdAt", Sort.Direction.DESC));
        verifyNoInteractions(journals);
    }
}
