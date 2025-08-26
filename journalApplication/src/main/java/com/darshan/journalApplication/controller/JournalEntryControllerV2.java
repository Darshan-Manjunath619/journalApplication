package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.JournalEntryService;
import com.darshan.journalApplication.service.UserEntryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/journal")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserEntryService userEntryService;

    // Get all entries
    @GetMapping("{userName}")
    public ResponseEntity<?> getAllJournalEntriesByUser(@PathVariable String userName) {
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
        @PostMapping("{userName}")
        public ResponseEntity<JournalEntry> createEntry (@RequestBody JournalEntry myEntry , @PathVariable String userName){
            try {
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
            Optional<JournalEntry> journalEntry = journalEntryService.getById(id);
            if (journalEntry.isPresent()) {
                return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            }
        }


        // Update entry by id
        @PutMapping("{userName}/{id}")
        public ResponseEntity<?> putById (@PathVariable ObjectId id,
                                          @RequestBody JournalEntry newEntry,
                                          @PathVariable String userName
        ){
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
                return new ResponseEntity<>(oldEntry ,HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }

       //Delete entry by id

        @DeleteMapping("{userName}/{id}")
        public ResponseEntity<?> deleteById (@PathVariable ObjectId id ,@PathVariable String userName){
            journalEntryService.deleteById(id , userName);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
