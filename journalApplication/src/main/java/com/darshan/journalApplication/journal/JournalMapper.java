package com.darshan.journalApplication.journal;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.dto.*;
import org.springframework.stereotype.Component;

@Component
public class JournalMapper {
    public JournalEntry toEntity(CreateJournalRequest request) {
        return JournalEntry.builder().title(request.title().trim())
                .content(request.content()).build();
    }

    public JournalResponse toResponse(JournalEntry entry) {
        return new JournalResponse(entry.getId(), entry.getTitle(),
                entry.getContent(), entry.getDate());
    }
}
