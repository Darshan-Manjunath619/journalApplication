package com.darshan.journalApplication.journal.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record UpdateJournalRequest(
        @Size(min = 1, max = 160) String title,
        @Size(max = 20000) String content,
        Boolean favorite,
        Set<@Positive Long> tagIds
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean isAnyFieldProvided() {
        return title != null || content != null || favorite != null || tagIds != null;
    }
}
