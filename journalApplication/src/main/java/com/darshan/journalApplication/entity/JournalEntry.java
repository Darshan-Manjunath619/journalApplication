package com.darshan.journalApplication.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
//Verifying from git

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "journal_entries")
//This annonation is used to tell springboot this is a collection that will be stored as mongodb document
@Data
// Just adding this will add all the gettter and setter.

public class
JournalEntry {
    @Id
    private ObjectId id;

    private String title;

    private String content;

    private LocalDateTime date;

}
