package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    // Add any custom queries if needed
}
