package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.service.JournalEntryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/journal")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    // Get all entries
    @GetMapping
    public List<JournalEntry> getAll() {
        return journalEntryService.getAll();
    }

    // Create new entry
    @PostMapping
    public JournalEntry createEntry(@RequestBody JournalEntry myEntry) {
        myEntry.setDate(LocalDateTime.now()); // Use java.util.Date for consistency with entity
        journalEntryService.EntryRecord(myEntry);
        return myEntry;
    }

    // Get request with path variable (id)
    @GetMapping("/{id}")
    public Optional<JournalEntry> getById(@PathVariable ObjectId id) {
        return journalEntryService.getById(id);
    }

    // Search by title (for future implementation)
    @GetMapping("/search")
    public List<JournalEntry> searchByTitle(@RequestParam String title) {
        // TODO: implement search logic later
        return Collections.emptyList();
    }

    // Update entry by id
    @PutMapping("/{id}")
    public Optional<JournalEntry> putById(@PathVariable ObjectId id, @RequestBody JournalEntry newEntry) {
        Optional<JournalEntry> oldEntryOpt = journalEntryService.getById(id);

        if (oldEntryOpt.isPresent()) {
            JournalEntry oldEntry = oldEntryOpt.get();

            if (newEntry.getTitle() != null && !newEntry.getTitle().isEmpty()) {
                oldEntry.setTitle(newEntry.getTitle());
            }

            if (newEntry.getContent() != null && !newEntry.getContent().isEmpty()) {
                oldEntry.setContent(newEntry.getContent());
            }

            journalEntryService.EntryRecord(oldEntry);
            return Optional.of(oldEntry);
        }
        return Optional.empty();
    }

    // Delete entry by id
    @DeleteMapping("/{id}")
    public boolean deleteById(@PathVariable ObjectId id) {
        journalEntryService.deleteById(id);
        return true;
    }
}
