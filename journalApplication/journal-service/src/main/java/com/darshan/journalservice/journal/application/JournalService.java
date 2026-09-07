package com.darshan.journalservice.journal.application;

import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.journal.persistence.JournalEntryRepository;
import com.darshan.journalservice.shared.error.InvalidQueryParameterException;
import com.darshan.journalservice.shared.error.ResourceNotFoundException;
import com.darshan.journalservice.tag.application.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class JournalService {
    private final JournalEntryRepository journals;
    private final TagService tags;

    public JournalService(JournalEntryRepository journals, TagService tags) {
        this.journals = journals;
        this.tags = tags;
    }

    @Transactional
    public JournalEntry create(JournalEntry entry, Long ownerId, Set<Long> tagIds) {
        entry.setOwnerId(ownerId);
        entry.setDate(LocalDateTime.now());
        entry.setTags(tags.resolveOwned(tagIds, ownerId));
        return journals.save(entry);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> listOwned(Long ownerId) {
        return journals.findAllByOwnerIdOrderByDateDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public Page<JournalEntry> searchOwned(Long ownerId, JournalSearchCriteria criteria,
                                          int page, int size, String sortField,
                                          Sort.Direction direction) {
        if (criteria.from() != null && criteria.to() != null
                && criteria.from().isAfter(criteria.to())) {
            throw new InvalidQueryParameterException("from must be before or equal to to");
        }

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
        Page<JournalEntry> result = journals.findAll(specification, pageable);
        result.getContent().forEach(entry -> entry.getTags().size());
        return result;
    }

    @Transactional(readOnly = true)
    public JournalEntry getOwned(Long id, Long ownerId) {
        return journals.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
    }

    @Transactional
    public JournalEntry update(Long id, Long ownerId, UpdateJournalCommand command) {
        JournalEntry entry = getOwned(id, ownerId);
        if (command.title() != null) entry.setTitle(command.title().trim());
        if (command.content() != null) entry.setContent(command.content());
        if (command.favorite() != null) entry.setFavorite(command.favorite());
        if (command.tagIds() != null) {
            entry.setTags(tags.resolveOwned(command.tagIds(), ownerId));
        }
        return journals.save(entry);
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        journals.delete(getOwned(id, ownerId));
    }
}
