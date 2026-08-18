package com.darshan.journalApplication.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record UpdateJournalRequest(
        @Schema(example = "Updated transaction notes")
        @Size(min = 1, max = 160) String title,
        @Schema(example = "Updated journal content")
        @Size(max = 20000) String content,
        @Schema(example = "true")
        Boolean favorite,
        @Schema(example = "[1, 3]", description = "Replaces tag assignments; an empty set clears them")
        Set<@Positive Long> tagIds
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean isAnyFieldProvided() {
        return title != null || content != null || favorite != null || tagIds != null;
    }
}
