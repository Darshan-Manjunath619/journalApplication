package com.darshan.journalApplication.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
//Verifying from git

import java.util.Date;

@Document(collation = "journal_entries")
//This annonation is used to tell springboot this is a collection that will be stored as mongodb document

public class JournalEntry {
    @Id
    private String id;

    private String title;

    private String content;

    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
