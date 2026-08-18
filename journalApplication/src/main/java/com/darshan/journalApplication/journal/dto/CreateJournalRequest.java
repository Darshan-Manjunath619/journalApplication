package com.darshan.journalApplication.journal.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateJournalRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 20000) String content,
        Set<@Positive Long> tagIds
) {}
