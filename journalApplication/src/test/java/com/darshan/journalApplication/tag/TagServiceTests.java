package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.shared.error.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceTests {
    @Mock TagRepository repository;
    @Mock JournalEntryRepository journals;
    @Mock UserEntryService users;
    TagService service;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TagService(repository, journals, users);
    }

    @Test void createTrimsAndNormalizesName() {
        User alice = User.builder().id(1L).userName("alice").build();
        when(users.getProfile("alice")).thenReturn(alice);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        Tag result = service.create("alice", " Spring ");
        assertEquals("Spring", result.getName());
        assertEquals("spring", result.getNormalizedName());
        assertSame(alice, result.getUser());
    }

    @Test void duplicateNormalizedNameIsConflict() {
        when(users.getProfile("alice")).thenReturn(User.builder().id(1L).build());
        when(repository.findByUserUserNameAndNormalizedName("alice", "spring"))
                .thenReturn(Optional.of(tag(2L, "Spring", "spring")));
        assertThrows(ConflictException.class, () -> service.create("alice", " SPRING "));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void renameAllowsSameTagButRejectsAnotherDuplicate() {
        Tag current = tag(1L, "Spring", "spring");
        when(repository.findByIdAndUserUserName(1L, "alice")).thenReturn(Optional.of(current));
        when(repository.findByUserUserNameAndNormalizedName("alice", "spring"))
                .thenReturn(Optional.of(current));
        when(repository.saveAndFlush(current)).thenReturn(current);
        assertSame(current, service.rename(1L, "alice", "Spring"));
        when(repository.findByUserUserNameAndNormalizedName("alice", "java"))
                .thenReturn(Optional.of(tag(2L, "Java", "java")));
        assertThrows(ConflictException.class, () -> service.rename(1L, "alice", "Java"));
    }

    @Test void resolveOwnedRejectsMissingOrCrossUserTag() {
        Set<Long> requested = Set.of(1L, 2L);
        when(repository.findAllByIdInAndUserUserName(requested, "alice"))
                .thenReturn(List.of(tag(1L, "Spring", "spring")));
        assertThrows(ResourceNotFoundException.class,
                () -> service.resolveOwned(requested, "alice"));
    }

    @Test void assignedTagCannotBeDeleted() {
        Tag tag = tag(1L, "Spring", "spring");
        when(repository.findByIdAndUserUserName(1L, "alice")).thenReturn(Optional.of(tag));
        when(journals.existsByTagsId(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L, "alice"));
        verify(repository, never()).delete(any());
    }

    @Test void unassignedOwnedTagIsDeleted() {
        Tag tag = tag(1L, "Spring", "spring");
        when(repository.findByIdAndUserUserName(1L, "alice")).thenReturn(Optional.of(tag));
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
