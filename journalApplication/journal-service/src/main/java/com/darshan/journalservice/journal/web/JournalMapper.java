package com.darshan.journalservice.journal.web;

import com.darshan.journalservice.journal.application.UpdateJournalCommand;
import com.darshan.journalservice.journal.domain.JournalEntry;
import com.darshan.journalservice.tag.web.TagMapper;
import com.darshan.journalservice.tag.web.TagResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class JournalMapper {
    private final TagMapper tags;

    public JournalMapper(TagMapper tags) {
        this.tags = tags;
    }

    public JournalEntry toEntity(CreateJournalRequest request) {
        JournalEntry entry = new JournalEntry();
        entry.setTitle(request.title().trim());
        entry.setContent(request.content());
        return entry;
    }

    public UpdateJournalCommand toCommand(UpdateJournalRequest request) {
        return new UpdateJournalCommand(request.title(), request.content(),
                request.favorite(), request.tagIds());
    }

    public JournalResponse toResponse(JournalEntry entry) {
        LinkedHashSet<TagResponse> tagResponses = entry.getTags().stream()
                .map(tags::toResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new JournalResponse(entry.getId(), entry.getTitle(), entry.getContent(),
                entry.getDate(), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.isFavorite(), tagResponses);
    }
}
