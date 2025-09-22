package com.darshan.journalApplication.entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "users")
@Data
public class User {
    @Id
    private ObjectId  id;
    //to automatically handle the unique we add to the properties
    @Indexed(unique = true)
    @NonNull
    private String userName;
    @NonNull
    private String password;
    private List<String> role;
    @DBRef
    private List<JournalEntry> journalEntries = new ArrayList<>();

}


