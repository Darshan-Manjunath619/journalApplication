package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.shared.error.ConflictException;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagService {
    private final TagRepository tags;
    private final JournalEntryRepository journals;
    private final UserEntryService users;

    public TagService(TagRepository tags, JournalEntryRepository journals,
                      UserEntryService users) {
        this.tags = tags;
        this.journals = journals;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Tag> listOwned(String userName) {
        return tags.findAllByUserUserNameOrderByNormalizedName(userName);
    }

    @Transactional
    public Tag create(String userName, String requestedName) {
        User user = users.getProfile(userName);
        Tag tag = new Tag();
        applyName(tag, requestedName, userName, null);
        tag.setUser(user);
        try {
            return tags.saveAndFlush(tag);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Tag name already exists");
        }
    }

    @Transactional
    public Tag rename(Long id, String userName, String requestedName) {
        Tag tag = getOwned(id, userName);
        applyName(tag, requestedName, userName, id);
        try {
            return tags.saveAndFlush(tag);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Tag name already exists");
        }
    }

    @Transactional
    public void delete(Long id, String userName) {
        Tag tag = getOwned(id, userName);
        if (journals.existsByTagsId(id)) {
            throw new ConflictException("Tag is assigned to one or more journal entries");
        }
        tags.delete(tag);
    }

    @Transactional(readOnly = true)
    public Set<Tag> resolveOwned(Set<Long> ids, String userName) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        List<Tag> owned = tags.findAllByIdInAndUserUserName(ids, userName);
        if (owned.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more tags were not found");
        }
        return new LinkedHashSet<>(owned);
    }

    private Tag getOwned(Long id, String userName) {
        return tags.findByIdAndUserUserName(id, userName)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    private void applyName(Tag tag, String requestedName, String userName, Long currentId) {
        String displayName = requestedName.trim();
        String normalized = displayName.toLowerCase(Locale.ROOT);
        tags.findByUserUserNameAndNormalizedName(userName, normalized)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Tag name already exists");
                });
        tag.setName(displayName);
        tag.setNormalizedName(normalized);
    }
}
