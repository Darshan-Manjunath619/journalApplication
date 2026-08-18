package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>,
        JpaSpecificationExecutor<JournalEntry> {
    List<JournalEntry> findAllByUserUserNameOrderByDateDesc(String userName);

    Optional<JournalEntry> findByIdAndUserUserName(Long id, String userName);
}
