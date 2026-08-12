package com.darshan.journalApplication.service;
import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.darshan.journalApplication.shared.error.ResourceNotFoundException;
import com.darshan.journalApplication.journal.dto.UpdateJournalRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final UserEntryService userEntryService;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               UserEntryService userEntryService) {
        this.journalEntryRepository = journalEntryRepository;
        this.userEntryService = userEntryService;
    }

    @Transactional
    public JournalEntry EntryRecord(JournalEntry journalEntry, String userName) {
            User user = userEntryService.findByUserName(userName);
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUser(user);
            JournalEntry saved = journalEntryRepository.save(journalEntry);
            user.getJournalEntries().add(saved);
            userEntryService.saveEntry(user);
            return saved;
    }

    public void EntryRecord(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);

    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public List<JournalEntry> getAllByOwner(String userName) {
        return journalEntryRepository.findAllByUserUserNameOrderByDateDesc(userName);
    }

    public Optional<JournalEntry> getById(Long id) {
        return journalEntryRepository.findById(id);
    }

    public JournalEntry getOwned(Long id, String userName) {
        return journalEntryRepository.findByIdAndUserUserName(id, userName)
                .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
    }

    @Transactional
    public JournalEntry updateOwned(Long id, String userName, UpdateJournalRequest request) {
        JournalEntry entry = getOwned(id, userName);
        if (request.title() != null) entry.setTitle(request.title().trim());
        if (request.content() != null) entry.setContent(request.content());
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public void deleteById(Long id, String userName) {
        JournalEntry entry = getOwned(id, userName);
        User user = entry.getUser();
        user.getJournalEntries().remove(entry);
        journalEntryRepository.delete(entry);
    }
}
