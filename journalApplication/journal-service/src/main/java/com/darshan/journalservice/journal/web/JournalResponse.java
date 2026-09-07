package com.darshan.journalservice.journal.web;

import com.darshan.journalservice.tag.web.TagResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

public record JournalResponse(Long id, String title, String content,
                              LocalDateTime date, Instant createdAt, Instant updatedAt,
                              boolean favorite, Set<TagResponse> tags) {
}
