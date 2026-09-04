package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.journal.port.JournalOwnerIdentityPort;
import com.darshan.journalApplication.journal.persistence.JournalEntryRepository;
import com.darshan.journalApplication.shared.error.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceTests {
    @Mock TagRepository repository;
    @Mock JournalEntryRepository journals;
    @Mock JournalOwnerIdentityPort ownerIdentity;
    TagService service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ownerIdentity.requireOwnerId("alice")).thenReturn(1L);
        service = new TagService(repository, journals, ownerIdentity);
    }

    @Test void createTrimsAndNormalizesName() {
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        Tag result = service.create("alice", " Spring ");
        assertEquals("Spring", result.getName());
        assertEquals("spring", result.getNormalizedName());
        assertEquals(1L, result.getOwnerId());
    }

    @Test void duplicateNormalizedNameIsConflict() {
        when(repository.findByOwnerIdAndNormalizedName(1L, "spring"))
                .thenReturn(Optional.of(tag(2L, "Spring", "spring")));
        assertThrows(ConflictException.class, () -> service.create("alice", " SPRING "));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void renameAllowsSameTagButRejectsAnotherDuplicate() {
        Tag current = tag(1L, "Spring", "spring");
        current.setOwnerId(1L);
        when(repository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(current));
        when(repository.findByOwnerIdAndNormalizedName(1L, "spring"))
                .thenReturn(Optional.of(current));
        when(repository.saveAndFlush(current)).thenReturn(current);
        assertSame(current, service.rename(1L, "alice", "Spring"));
        when(repository.findByOwnerIdAndNormalizedName(1L, "java"))
                .thenReturn(Optional.of(tag(2L, "Java", "java")));
        assertThrows(ConflictException.class, () -> service.rename(1L, "alice", "Java"));
    }

    @Test void resolveOwnedRejectsMissingOrCrossUserTag() {
        Set<Long> requested = Set.of(1L, 2L);
        when(repository.findAllByIdInAndOwnerId(requested, 1L))
                .thenReturn(List.of(tag(1L, "Spring", "spring")));
        assertThrows(ResourceNotFoundException.class,
                () -> service.resolveOwned(requested, "alice"));
    }

    @Test void assignedTagCannotBeDeleted() {
        Tag tag = tag(1L, "Spring", "spring");
        when(repository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(tag));
        when(journals.existsByTagsId(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L, "alice"));
        verify(repository, never()).delete(any());
    }

    @Test void unassignedOwnedTagIsDeleted() {
        Tag tag = tag(1L, "Spring", "spring");
        when(repository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(tag));
        service.delete(1L, "alice");
        verify(repository).delete(tag);
    }

    private Tag tag(Long id, String name, String normalizedName) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setNormalizedName(normalizedName);
        return tag;
    }
}
