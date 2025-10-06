package com.darshan.journalApplication.entity;

import lombok.*;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId  id;
    //to automatically handle the unique we add to the properties
    @Indexed(unique = true)
    @NonNull
    private String userName;
    @NonNull
    private String password;
    private String email;
    private boolean sentimentAnalysis;
    private List<String> role;
    @DBRef
    private List<JournalEntry> journalEntries = new ArrayList<>();

}


