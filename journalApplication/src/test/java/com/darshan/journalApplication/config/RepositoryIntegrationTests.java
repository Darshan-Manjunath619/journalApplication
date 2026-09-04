package com.darshan.journalApplication.config;

import com.darshan.journalApplication.entity.*;
import com.darshan.journalApplication.journal.JournalSearchCriteria;
import com.darshan.journalApplication.journal.dto.UpdateJournalRequest;
import com.darshan.journalApplication.repository.*;
import com.darshan.journalApplication.service.JournalEntryService;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import com.darshan.journalApplication.tag.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RepositoryIntegrationTests {
    @Autowired UserEntryRepository users;
    @Autowired TagRepository tags;
    @Autowired JournalEntryRepository journals;
    @Autowired JournalEntryService journalService;

    @Test void combinedFiltersRemainOwnerScoped() {
        User alice = user("repo_filter_alice");
        User bob = user("repo_filter_bob");
        Tag aliceSpring = tag(alice, "Spring", "spring");
        Tag bobSpring = tag(bob, "Spring", "spring");
        journal(alice, "Spring Deep Dive", "JPA notes", true, aliceSpring);
        journal(alice, "Spring Weekend", "Personal", false, aliceSpring);
        journal(bob, "Spring Private", "Bob only", true, bobSpring);

        Page<JournalEntry> result = journalService.searchOwned(alice.getUserName(),
                new JournalSearchCriteria("spring", Instant.parse("2000-01-01T00:00:00Z"),
                        Instant.parse("2100-01-01T00:00:00Z"), true, aliceSpring.getId()),
                0, 20, "createdAt", Sort.Direction.DESC);

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Deep Dive", result.getContent().get(0).getTitle());
        assertEquals(alice.getId(), result.getContent().get(0).getOwnerId());
    }

    @Test void stableIdTieBreakerKeepsPageBoundariesDeterministic() {
        User owner = user("repo_pages");
        List<Long> ids = new ArrayList<>();
        ids.add(journal(owner, "Same", "One", false).getId());
        ids.add(journal(owner, "Same", "Two", false).getId());
        ids.add(journal(owner, "Same", "Three", false).getId());
        Collections.sort(ids);

        List<Long> paged = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            Page<JournalEntry> result = journalService.searchOwned(owner.getUserName(),
                    new JournalSearchCriteria(null, null, null, null, null),
                    page, 1, "title", Sort.Direction.ASC);
            paged.add(result.getContent().get(0).getId());
        }

        assertEquals(ids, paged);
    }

    @Test void failedTagResolutionRollsBackEarlierJournalChanges() {
        User owner = user("repo_rollback");
        JournalEntry entry = journal(owner, "Original", "Body", false);

        assertThrows(ResourceNotFoundException.class, () -> journalService.updateOwned(
                entry.getId(), owner.getUserName(),
                new UpdateJournalRequest("Changed", null, null, Set.of(Long.MAX_VALUE))));

        JournalEntry reloaded = journals.findById(entry.getId()).orElseThrow();
        assertEquals("Original", reloaded.getTitle());
    }

    @Test void databaseEnforcesNormalizedTagUniquenessPerUser() {
        User owner = user("repo_unique_tag");
        tag(owner, "Spring", "spring");

        assertThrows(DataIntegrityViolationException.class,
                () -> tag(owner, "SPRING", "spring"));
    }

    @Test void deletingUserCascadesOwnedJournalAndTagData() {
        User owner = user("repo_cascade");
        Tag tag = tag(owner, "Spring", "spring");
        JournalEntry journal = journal(owner, "Owned", "Body", false, tag);

        users.deleteById(owner.getId());
        users.flush();

        assertFalse(journals.existsById(journal.getId()));
        assertFalse(tags.existsById(tag.getId()));
    }

    private User user(String name) {
        return users.save(User.builder().userName(name)
                .email(name + "@example.com").password("test-hash")
                .role(List.of("USER")).build());
    }

    private Tag tag(User user, String name, String normalizedName) {
        Tag tag = new Tag();
        tag.setOwnerId(user.getId());
        tag.setName(name);
        tag.setNormalizedName(normalizedName);
        return tags.saveAndFlush(tag);
    }

    private JournalEntry journal(User user, String title, String content,
                                 boolean favorite, Tag... assignedTags) {
        JournalEntry entry = JournalEntry.builder().title(title).content(content)
                .favorite(favorite).ownerId(user.getId())
                .date(java.time.LocalDateTime.now())
                .tags(new LinkedHashSet<>(List.of(assignedTags))).build();
        return journals.saveAndFlush(entry);
    }
}
