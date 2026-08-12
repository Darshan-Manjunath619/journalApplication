package com.darshan.journalApplication.journal.dto;

import jakarta.validation.constraints.*;

public record CreateJournalRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 20000) String content
) {}
