package com.darshan.journalApplication.journal.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record UpdateJournalRequest(
        @Size(min = 1, max = 160) String title,
        @Size(max = 20000) String content
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean isAnyFieldProvided() {
        return title != null || content != null;
    }
}
