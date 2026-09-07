package com.darshan.journalservice.persistence;

import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.journal.application.JournalService;
import com.darshan.journalservice.journal.application.UpdateJournalCommand;
import com.darshan.journalservice.journal.persistence.JournalEntryRepository;
import com.darshan.journalservice.tag.domain.Tag;
import com.darshan.journalservice.tag.application.TagService;
import com.darshan.journalservice.tag.persistence.TagRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JournalPersistenceTests {
    @Autowired JournalEntryRepository journals;
    @Autowired TagRepository tags;
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;
    @Autowired JournalService journalService;
    @Autowired TagService tagService;

    @Test
    void migrationCreatesOnlyJournalOwnedTables() {
        assertEquals("1", flyway.info().current().getVersion().getVersion());
        assertEquals(0, tableCount("users"));
        assertEquals(1, tableCount("journal_entries"));
        assertEquals(1, tableCount("tags"));
        assertEquals(1, tableCount("journal_entry_tags"));
        assertEquals(1, tableCount("flyway_schema_history"));
    }

    @Test
    void repositoriesKeepJournalAndTagsOwnerScoped() {
        Tag tag = new Tag();
        tag.setOwnerId(42L);
        tag.setName("Spring");
        tag.setNormalizedName("spring");
        tag = tags.saveAndFlush(tag);

        JournalEntry entry = JournalEntry.builder()
                .ownerId(42L).title("Learning").content("Notes")
                .date(LocalDateTime.now())
                .tags(new LinkedHashSet<>(List.of(tag))).build();
        entry = journals.saveAndFlush(entry);

        assertTrue(journals.findByIdAndOwnerId(entry.getId(), 42L).isPresent());
        assertTrue(journals.findByIdAndOwnerId(entry.getId(), 99L).isEmpty());
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getUpdatedAt());
    }

    @Test
    void failedTagAssignmentRollsBackJournalUpdate() {
        Tag tag = tagService.create(77L, "Java");
        JournalEntry entry = journalService.create(
                JournalEntry.builder().title("Original").content("Body").build(),
                77L, java.util.Set.of(tag.getId()));

        assertThrows(com.darshan.journalservice.shared.error.ResourceNotFoundException.class,
                () -> journalService.update(entry.getId(), 77L,
                        new UpdateJournalCommand("Changed", null, null,
                                java.util.Set.of(Long.MAX_VALUE))));

        JournalEntry reloaded = journals.findById(entry.getId()).orElseThrow();
        assertEquals("Original", reloaded.getTitle());
    }

    private int tableCount(String name) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = SCHEMA() AND LOWER(TABLE_NAME) = ?",
                Integer.class, name);
        return count == null ? 0 : count;
    }
}
