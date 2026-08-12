package com.darshan.journalApplication.journal.dto;

import java.time.LocalDateTime;

public record JournalResponse(Long id, String title, String content,
                              LocalDateTime date) {}
