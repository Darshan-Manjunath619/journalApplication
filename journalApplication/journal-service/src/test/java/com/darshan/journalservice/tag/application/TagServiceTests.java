package com.darshan.journalservice.tag.application;

import com.darshan.journalservice.journal.persistence.JournalEntryRepository;
import com.darshan.journalservice.shared.error.ConflictException;
import com.darshan.journalservice.shared.error.ResourceNotFoundException;
import com.darshan.journalservice.tag.domain.Tag;
import com.darshan.journalservice.tag.persistence.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceTests {
    private final TagRepository tags = mock(TagRepository.class);
    private final JournalEntryRepository journals = mock(JournalEntryRepository.class);
    private TagService service;

    @BeforeEach
    void setUp() {
        service = new TagService(tags, journals);
    }

    @Test
    void createTrimsNormalizesAndAssignsOwner() {
        when(tags.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));

        Tag result = service.create(42L, " Spring ");

        assertEquals(42L, result.getOwnerId());
        assertEquals("Spring", result.getName());
        assertEquals("spring", result.getNormalizedName());
    }

    @Test
    void duplicateNormalizedNameIsConflictForSameOwner() {
        when(tags.findByOwnerIdAndNormalizedName(42L, "spring"))
                .thenReturn(Optional.of(tag(1L, 42L, "Spring", "spring")));

        assertThrows(ConflictException.class,
                () -> service.create(42L, " SPRING "));
        verify(tags, never()).saveAndFlush(any());
    }

    @Test
    void missingOrCrossOwnerTagAssignmentIsRejected() {
        Set<Long> requested = Set.of(1L, 2L);
        when(tags.findAllByIdInAndOwnerId(requested, 42L))
                .thenReturn(List.of(tag(1L, 42L, "Spring", "spring")));

        assertThrows(ResourceNotFoundException.class,
                () -> service.resolveOwned(requested, 42L));
    }

    @Test
    void assignedTagCannotBeDeleted() {
        Tag tag = tag(1L, 42L, "Spring", "spring");
        when(tags.findByIdAndOwnerId(1L, 42L)).thenReturn(Optional.of(tag));
        when(journals.existsByTagsId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.delete(1L, 42L));
        verify(tags, never()).delete(any());
    }

    private Tag tag(Long id, Long ownerId, String name, String normalizedName) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setOwnerId(ownerId);
        tag.setName(name);
        tag.setNormalizedName(normalizedName);
        return tag;
    }
}
