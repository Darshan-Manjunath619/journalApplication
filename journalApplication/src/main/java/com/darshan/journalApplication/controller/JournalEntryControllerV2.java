package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public JournalEntry getById(@PathVariable Long id){
            return null;
        }


        // Get Request with request paramater
        @GetMapping("/search")
        public List<JournalEntry> searchByTitle(@RequestParam String title) {
            return null;

        }

        // put request with parameter
        @PutMapping("{id}")
        public JournalEntry putById(@PathVariable Long id , @RequestBody JournalEntry myEntry){
            return null;
        }

        // delete request with parameter
        @DeleteMapping("{id}")
        public JournalEntry deleteById(@PathVariable Long id){
            return null;
        }

    }

