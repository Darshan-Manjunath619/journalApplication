package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.port.JournalOwnerIdentityPort;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import com.darshan.journalApplication.journal.dto.UpdateJournalRequest;
import com.darshan.journalApplication.journal.JournalSearchCriteria;
import com.darshan.journalApplication.shared.error.InvalidQueryParameterException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import com.darshan.journalApplication.tag.TagService;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalOwnerIdentityPort ownerIdentity;
    private final TagService tagService;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               JournalOwnerIdentityPort ownerIdentity, TagService tagService) {
        this.journalEntryRepository = journalEntryRepository;
        this.ownerIdentity = ownerIdentity;
        this.tagService = tagService;
    }

    @Transactional
    public JournalEntry createOwned(JournalEntry journalEntry, String userName) {
        return createOwned(journalEntry, userName, Set.of());
    }

    @Transactional
    public JournalEntry createOwned(JournalEntry journalEntry, String userName, Set<Long> tagIds) {
        Long ownerId = ownerIdentity.requireOwnerId(userName);
        journalEntry.setDate(LocalDateTime.now());
        journalEntry.setOwnerId(ownerId);
        journalEntry.setTags(tagService.resolveOwned(tagIds, userName));
        return journalEntryRepository.save(journalEntry);
    }

    /** Legacy API compatibility; use createOwned for new code. */
    public JournalEntry EntryRecord(JournalEntry journalEntry, String userName) {
        return createOwned(journalEntry, userName);
    }

    public void EntryRecord(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);

    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> getAllByOwner(String userName) {
        return journalEntryRepository.findAllByOwnerIdOrderByDateDesc(
                ownerIdentity.requireOwnerId(userName));
    }

    @Transactional(readOnly = true)
    public Page<JournalEntry> searchOwned(String userName, JournalSearchCriteria criteria,
                                          int page, int size, String sortField,
                                          Sort.Direction direction) {
        if (criteria.from() != null && criteria.to() != null
                && criteria.from().isAfter(criteria.to())) {
            throw new InvalidQueryParameterException("from must be before or equal to to");
        }

        Long ownerId = ownerIdentity.requireOwnerId(userName);
        Specification<JournalEntry> specification = (root, query, builder) -> {
            query.distinct(true);
            var predicate = builder.equal(root.get("ownerId"), ownerId);
            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("content")), pattern)));
            }
            if (criteria.from() != null) {
                predicate = builder.and(predicate,
                        builder.greaterThanOrEqualTo(root.get("createdAt"), criteria.from()));
            }
            if (criteria.to() != null) {
                predicate = builder.and(predicate,
                        builder.lessThanOrEqualTo(root.get("createdAt"), criteria.to()));
            }
            if (criteria.favorite() != null) {
                predicate = builder.and(predicate,
                        builder.equal(root.get("favorite"), criteria.favorite()));
            }
            if (criteria.tagId() != null) {
                predicate = builder.and(predicate,
                        builder.equal(root.join("tags").get("id"), criteria.tagId()));
            }
            return predicate;
        };

        Sort stableSort = Sort.by(direction, sortField).and(Sort.by(direction, "id"));
        Pageable pageable = PageRequest.of(page, size, stableSort);
        Page<JournalEntry> result = journalEntryRepository.findAll(specification, pageable);
        result.getContent().forEach(entry -> entry.getTags().size());
        return result;
    }

    public Optional<JournalEntry> getById(Long id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public JournalEntry getOwned(Long id, String userName) {
        return journalEntryRepository.findByIdAndOwnerId(
                        id, ownerIdentity.requireOwnerId(userName))
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
    }

    @Transactional
    public JournalEntry updateOwned(Long id, String userName, UpdateJournalRequest request) {
        JournalEntry entry = getOwned(id, userName);
        if (request.title() != null) entry.setTitle(request.title().trim());
        if (request.content() != null) entry.setContent(request.content());
        if (request.favorite() != null) entry.setFavorite(request.favorite());
        if (request.tagIds() != null) {
            entry.setTags(tagService.resolveOwned(request.tagIds(), userName));
        }
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public void deleteOwned(Long id, String userName) {
        JournalEntry entry = getOwned(id, userName);
        journalEntryRepository.delete(entry);
    }

    /** Legacy API compatibility; use deleteOwned for new code. */
    public void deleteById(Long id, String userName) {
        deleteOwned(id, userName);
    }
}
