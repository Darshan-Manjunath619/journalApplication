package com.darshan.journalApplication.journal.dto;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Set;
import com.darshan.journalApplication.tag.dto.TagResponse;

public record JournalResponse(Long id, String title, String content,
                              LocalDateTime date, Instant createdAt, Instant updatedAt,
                              boolean favorite, Set<TagResponse> tags) {}
