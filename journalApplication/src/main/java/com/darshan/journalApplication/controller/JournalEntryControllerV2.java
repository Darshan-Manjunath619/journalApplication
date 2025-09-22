package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.JournalEntryService;
import com.darshan.journalApplication.service.UserEntryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserEntryService userEntryService;

    // Get all entries
    @GetMapping
    public ResponseEntity<?> getAllJournalEntriesByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userEntryService.findByUserName(userName);
        List<JournalEntry> all = user.getJournalEntries();
        if(all != null && !all.isEmpty()){
            return new ResponseEntity<>(all , HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

        // Create new entry
        @PostMapping
        public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry){
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userName = authentication.getName();
                myEntry.setDate(LocalDateTime.now()); // Use java.util.Date for consistency with entity
                journalEntryService.EntryRecord(myEntry,userName);
                return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }


        // Get request with path variable (id)
        @GetMapping("/id/{id}")
        public ResponseEntity<JournalEntry> getById (@PathVariable ObjectId id){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User byUserName = userEntryService.findByUserName(userName);
            List<JournalEntry> collect = byUserName.getJournalEntries().stream().filter(x -> x.getId().equals(id)).collect(Collectors.toList());
            if(!collect.isEmpty()) {
                Optional<JournalEntry> journalEntry = journalEntryService.getById(id);
                if (journalEntry.isPresent()) {
                    return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
                }
            }
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


        // Update entry by id
        @PutMapping("/{id}")
        public ResponseEntity<?> putById (@PathVariable ObjectId id,
                                          @RequestBody JournalEntry newEntry
        ){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User byUserName = userEntryService.findByUserName(userName);
            List<JournalEntry> collect = byUserName.getJournalEntries().stream().filter(x -> x.getId().equals(id)).collect(Collectors.toList());
            Optional<JournalEntry> oldEntryOpt = journalEntryService.getById(id);
            if(!collect.isEmpty()) {
                Optional<JournalEntry> journalEntry = journalEntryService.getById(id);
                if (journalEntry.isPresent()) {
                    JournalEntry oldEntry = oldEntryOpt.get();
                    if (newEntry.getTitle() != null && !newEntry.getTitle().isEmpty()) {
                        oldEntry.setTitle(newEntry.getTitle());
                    }
                    if (newEntry.getContent() != null && !newEntry.getContent().isEmpty()) {
                        oldEntry.setContent(newEntry.getContent());
                    }
                    journalEntryService.EntryRecord(oldEntry);
                    return new ResponseEntity<>(oldEntry ,HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }

       //Delete entry by id

        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteById (@PathVariable ObjectId id){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            journalEntryService.deleteById(id , userName);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


    }
