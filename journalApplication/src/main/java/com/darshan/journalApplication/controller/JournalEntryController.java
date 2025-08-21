package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/journal")

public class JournalEntryController {

    private Map<Long , JournalEntry> journalEntries = new HashMap<>();

    @GetMapping
    public List<JournalEntry> getAll(){
        return new ArrayList<>(journalEntries.values());
    }

    @PostMapping
    public boolean createEntry(@RequestBody JournalEntry myEntry){
        journalEntries.put(myEntry.getId() , myEntry);
        return true;
    }

    @GetMapping("/{id}")
    public JournalEntry getById(@PathVariable Long id){
            return journalEntries.get(id);
        }

    @GetMapping("/search")
    public List<JournalEntry> searchByTitle(@RequestParam String title) {
        List<JournalEntry> result = new ArrayList<>();
        for (JournalEntry entry : journalEntries.values()) {
            if (entry.getTitle().equalsIgnoreCase(title)) {
                result.add(entry);
            }
        }
        return result;
    }

    @DeleteMapping("{id}")
    public JournalEntry deleteById(@PathVariable Long id){
        return journalEntries.remove(id);
    }

    @PutMapping("{id}")
    public JournalEntry putById(@PathVariable Long id , @RequestBody JournalEntry myEntry){
        return journalEntries.put(id , myEntry);
    }

}
