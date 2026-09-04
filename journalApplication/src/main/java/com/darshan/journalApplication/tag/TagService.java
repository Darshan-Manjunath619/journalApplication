package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.journal.port.JournalOwnerIdentityPort;
import com.darshan.journalApplication.journal.persistence.JournalEntryRepository;
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
    private final JournalOwnerIdentityPort ownerIdentity;

    public TagService(TagRepository tags, JournalEntryRepository journals,
                      JournalOwnerIdentityPort ownerIdentity) {
        this.tags = tags;
        this.journals = journals;
        this.ownerIdentity = ownerIdentity;
    }

    @Transactional(readOnly = true)
    public List<Tag> listOwned(String userName) {
        return tags.findAllByOwnerIdOrderByNormalizedName(ownerIdentity.requireOwnerId(userName));
    }

    @Transactional
    public Tag create(String userName, String requestedName) {
        Long ownerId = ownerIdentity.requireOwnerId(userName);
        Tag tag = new Tag();
        applyName(tag, requestedName, ownerId, null);
        tag.setOwnerId(ownerId);
        try {
            return tags.saveAndFlush(tag);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Tag name already exists");
        }
    }

    @Transactional
    public Tag rename(Long id, String userName, String requestedName) {
        Tag tag = getOwned(id, userName);
        applyName(tag, requestedName, tag.getOwnerId(), id);
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
        Long ownerId = ownerIdentity.requireOwnerId(userName);
        List<Tag> owned = tags.findAllByIdInAndOwnerId(ids, ownerId);
        if (owned.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more tags were not found");
        }
        return new LinkedHashSet<>(owned);
    }

    private Tag getOwned(Long id, String userName) {
        return tags.findByIdAndOwnerId(id, ownerIdentity.requireOwnerId(userName))
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    private void applyName(Tag tag, String requestedName, Long ownerId, Long currentId) {
        String displayName = requestedName.trim();
        String normalized = displayName.toLowerCase(Locale.ROOT);
        tags.findByOwnerIdAndNormalizedName(ownerId, normalized)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Tag name already exists");
                });
        tag.setName(displayName);
        tag.setNormalizedName(normalized);
    }
}
