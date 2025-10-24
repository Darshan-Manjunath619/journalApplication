package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserEntryService userEntryService;

    @Transactional
    public void EntryRecord(JournalEntry journalEntry, String userName) {
            User user = userEntryService.findByUserName(userName);
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUser(user);
            JournalEntry saved = journalEntryRepository.save(journalEntry);
            user.getJournalEntries().add(saved);
            userEntryService.saveEntry(user);

    }

    public void EntryRecord(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);

    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> getById(Long id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public void deleteById(Long id, String userName) {
        try {
            User user = userEntryService.findByUserName(userName);
            boolean b = user.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if(b) {
                userEntryService.saveEntry(user);
                journalEntryRepository.deleteById(id);
        }
        } catch (Exception e) {
            log.error(String.valueOf(e + "check"));
            throw new RuntimeException("An Error Occured while deleting.");
        }
    }
}
