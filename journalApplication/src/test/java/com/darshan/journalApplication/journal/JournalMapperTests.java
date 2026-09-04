package com.darshan.journalApplication.journal;

import com.darshan.journalApplication.journal.domain.JournalEntry;
import com.darshan.journalApplication.journal.dto.*;
import com.darshan.journalApplication.tag.Tag;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashSet;
import static org.junit.jupiter.api.Assertions.*;

class JournalMapperTests {
    private final JournalMapper mapper = new JournalMapper();

    @Test void trimsCreateTitleAndDoesNotAcceptPersistenceRelationships() {
        JournalEntry entry = mapper.toEntity(
                new CreateJournalRequest(" Title ", "Body", java.util.Set.of(10L)));
        assertEquals("Title", entry.getTitle());
        assertEquals("Body", entry.getContent());
        assertNull(entry.getOwnerId());
        assertTrue(entry.getTags().isEmpty());
    }

    @Test void responseContainsSafeTagsInStableNormalizedOrder() {
        Tag spring = tag(2L, "Spring", "spring");
        Tag javaTag = tag(1L, "Java", "java");
        JournalEntry entry = JournalEntry.builder().id(5L).title("Learning")
                .content("Notes").favorite(true)
                .ownerId(9L)
                .tags(new LinkedHashSet<>(java.util.List.of(spring, javaTag))).build();
        JournalResponse response = mapper.toResponse(entry);
        assertTrue(response.favorite());
        assertEquals(java.util.List.of("Java", "Spring"),
                response.tags().stream().map(tag -> tag.name()).toList());
    }

    private Tag tag(Long id, String name, String normalizedName) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setNormalizedName(normalizedName);
        return tag;
    }
}
