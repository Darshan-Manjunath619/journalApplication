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

        @GetMapping
        public List<JournalEntry> getAll(){
            return journalEntryService.getAll();
        }

        @PostMapping
        public JournalEntry createEntry(@RequestBody JournalEntry myEntry){
            myEntry.setDate(LocalDateTime.now());
            journalEntryService.EntryRecord(myEntry);
            return myEntry;
        }

        // Get request with path(id)
        @GetMapping("/{id}")
        public Optional<JournalEntry> getById(@PathVariable ObjectId id){
            return journalEntryService.getById(id);
        }


        // Get Request with request paramater
        @GetMapping("/search")
        public List<JournalEntry> searchByTitle(@RequestParam String title) {
            return null;

        }

        // put request with parameter
        @PutMapping("{id}")
        public JournalEntry putById(@PathVariable ObjectId id , @RequestBody JournalEntry myEntry){
            return null;
        }

        // delete request with parameter
        @DeleteMapping("{id}")
        public JournalEntry deleteById(@PathVariable ObjectId id){
            return null;
        }

    }

