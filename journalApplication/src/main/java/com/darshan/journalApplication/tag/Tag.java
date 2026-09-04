package com.darshan.journalApplication.tag;

import com.darshan.journalApplication.journal.domain.JournalEntry;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints =
        @UniqueConstraint(name = "uk_tags_user_normalized_name",
                columnNames = {"user_id", "normalized_name"}))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 80)
    private String normalizedName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false)
    private Long ownerId;

    @ManyToMany(mappedBy = "tags")
    private Set<JournalEntry> journalEntries = new LinkedHashSet<>();
}
