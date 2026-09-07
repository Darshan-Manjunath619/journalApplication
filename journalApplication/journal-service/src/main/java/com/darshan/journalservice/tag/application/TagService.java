package com.darshan.journalservice.tag.application;

import com.darshan.journalservice.journal.persistence.JournalEntryRepository;
import com.darshan.journalservice.shared.error.ConflictException;
import com.darshan.journalservice.shared.error.ResourceNotFoundException;
import com.darshan.journalservice.tag.domain.Tag;
import com.darshan.journalservice.tag.persistence.TagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TagService {
    private final TagRepository tags;
    private final JournalEntryRepository journals;

    public TagService(TagRepository tags, JournalEntryRepository journals) {
        this.tags = tags;
        this.journals = journals;
    }

    @Transactional(readOnly = true)
    public List<Tag> listOwned(Long ownerId) {
        return tags.findAllByOwnerIdOrderByNormalizedName(ownerId);
    }

    @Transactional
    public Tag create(Long ownerId, String requestedName) {
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
    public Tag rename(Long id, Long ownerId, String requestedName) {
        Tag tag = getOwned(id, ownerId);
        applyName(tag, requestedName, ownerId, id);
        try {
            return tags.saveAndFlush(tag);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Tag name already exists");
        }
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        Tag tag = getOwned(id, ownerId);
        if (journals.existsByTagsId(id)) {
            throw new ConflictException("Tag is assigned to one or more journal entries");
        }
        tags.delete(tag);
    }

    @Transactional(readOnly = true)
    public Set<Tag> resolveOwned(Set<Long> ids, Long ownerId) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        List<Tag> owned = tags.findAllByIdInAndOwnerId(ids, ownerId);
        if (owned.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more tags were not found");
        }
        return new LinkedHashSet<>(owned);
    }

    private Tag getOwned(Long id, Long ownerId) {
        return tags.findByIdAndOwnerId(id, ownerId)
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
