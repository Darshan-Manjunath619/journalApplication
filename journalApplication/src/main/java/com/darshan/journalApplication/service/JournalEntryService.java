package com.darshan.journalApplication.service;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    public void EntryRecord(JournalEntry journalEntry){
        journalEntryRepository.save(journalEntry);
    }
}
