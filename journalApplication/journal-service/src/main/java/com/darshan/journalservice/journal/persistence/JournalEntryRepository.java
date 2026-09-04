package com.darshan.journalservice.journal.persistence;

import com.darshan.journalservice.journal.domain.JournalEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>,
        JpaSpecificationExecutor<JournalEntry> {
    List<JournalEntry> findAllByOwnerIdOrderByDateDesc(Long ownerId);

    @EntityGraph(attributePaths = "tags")
    Optional<JournalEntry> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByTagsId(Long tagId);
}
