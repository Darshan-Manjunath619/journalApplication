package com.darshan.journalApplication.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // Maps to the table 'users' in MySQL
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated primary key
    private Long id; // ObjectId → Long (MySQL PK)

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    private String email;

    private boolean sentimentAnalysis;

    @ElementCollection(fetch = FetchType.EAGER) // For storing list of roles as separate table
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> role = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntry> journalEntries = new ArrayList<>();
}
