package com.darshan.journalApplication.journal.application;

import com.darshan.journalApplication.journal.domain.JournalEntry;
import com.darshan.journalApplication.journal.dto.UpdateJournalRequest;
import com.darshan.journalApplication.journal.JournalSearchCriteria;
import com.darshan.journalApplication.journal.port.JournalOwnerIdentityPort;
import com.darshan.journalApplication.journal.persistence.JournalEntryRepository;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import com.darshan.journalApplication.shared.error.InvalidQueryParameterException;
import com.darshan.journalApplication.tag.Tag;
import com.darshan.journalApplication.tag.TagService;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JournalOwnershipTests {
    @Mock JournalEntryRepository repository;
    @Mock JournalOwnerIdentityPort ownerIdentity;
    @Mock TagService tags;
    JournalEntryService service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ownerIdentity.requireOwnerId("alice")).thenReturn(1L);
        service = new JournalEntryService(repository, ownerIdentity, tags);
    }

    @Test void missingOrUnownedEntryIsNotFound() {
        when(repository.findByIdAndOwnerId(9L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getOwned(9L, "alice"));
    }

    @Test void updateUsesOwnedQueryAndChangesOnlyProvidedFields() {
        JournalEntry entry = JournalEntry.builder().id(2L).title("Old").content("Keep").build();
        when(repository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(entry));
        when(repository.save(entry)).thenReturn(entry);
        JournalEntry result = service.updateOwned(2L, "alice",
                new UpdateJournalRequest(" New ", null, null, null));
        assertEquals("New", result.getTitle());
        assertEquals("Keep", result.getContent());
        verify(repository).findByIdAndOwnerId(2L, 1L);
    }

    @Test void combinedUpdateChangesContentFavoriteAndOwnedTags() {
        JournalEntry entry = JournalEntry.builder().id(2L).title("Keep")
                .content("Old").favorite(false).build();
        Tag tag = new Tag();
        tag.setId(7L);
        Set<Tag> resolved = new LinkedHashSet<>(List.of(tag));
        when(repository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(entry));
        when(tags.resolveOwned(Set.of(7L), "alice")).thenReturn(resolved);
        when(repository.save(entry)).thenReturn(entry);

        JournalEntry result = service.updateOwned(2L, "alice",
                new UpdateJournalRequest(null, "New", true, Set.of(7L)));

        assertEquals("Keep", result.getTitle());
        assertEquals("New", result.getContent());
        assertTrue(result.isFavorite());
        assertSame(resolved, result.getTags());
    }

    @Test void emptyTagIdsClearAssignments() {
        JournalEntry entry = JournalEntry.builder().id(2L).title("Keep").build();
        entry.getTags().add(new Tag());
        when(repository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(entry));
        when(tags.resolveOwned(Set.of(), "alice")).thenReturn(new LinkedHashSet<>());
        when(repository.save(entry)).thenReturn(entry);

        JournalEntry result = service.updateOwned(2L, "alice",
                new UpdateJournalRequest(null, null, null, Set.of()));

        assertTrue(result.getTags().isEmpty());
    }

    @Test void rejectsReversedDateRangeBeforeRepositoryQuery() {
        JournalSearchCriteria criteria = new JournalSearchCriteria(null,
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"), null, null);
        assertThrows(InvalidQueryParameterException.class, () ->
                service.searchOwned("alice", criteria, 0, 20,
                        "createdAt", Sort.Direction.DESC));
        verify(repository, never()).findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<JournalEntry>>any(),
                ArgumentMatchers.<org.springframework.data.domain.Pageable>any());
    }
}
