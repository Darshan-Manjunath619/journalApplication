package com.darshan.journalApplication.journal.dto;

import java.time.LocalDateTime;
import java.time.Instant;

public record JournalResponse(Long id, String title, String content,
                              LocalDateTime date, Instant createdAt, Instant updatedAt,
                              boolean favorite) {}
