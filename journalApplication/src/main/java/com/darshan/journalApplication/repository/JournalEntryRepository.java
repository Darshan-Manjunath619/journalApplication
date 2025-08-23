package com.darshan.journalApplication.repository;

import com.darshan.journalApplication.entity.JournalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface JournalEntryRepository extends MongoRepository<JournalEntry,String> {



}
