package com.darshan.journalApplication.journal;

import com.darshan.journalApplication.journal.domain.JournalEntry;
import com.darshan.journalApplication.journal.dto.*;
import org.springframework.stereotype.Component;
import com.darshan.journalApplication.tag.dto.TagResponse;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class JournalMapper {
    public JournalEntry toEntity(CreateJournalRequest request) {
        return JournalEntry.builder().title(request.title().trim())
                .content(request.content()).build();
    }

    public JournalResponse toResponse(JournalEntry entry) {
        return new JournalResponse(entry.getId(), entry.getTitle(),
                entry.getContent(), entry.getDate(), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.isFavorite(), entry.getTags().stream()
                        .sorted(java.util.Comparator.comparing(tag -> tag.getNormalizedName()))
                        .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
