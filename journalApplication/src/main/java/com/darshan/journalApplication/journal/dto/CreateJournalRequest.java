package com.darshan.journalApplication.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateJournalRequest(
        @Schema(example = "Learning Spring transactions")
        @NotBlank @Size(max = 160) String title,
        @Schema(example = "A transaction commits all changes or rolls them all back.")
        @Size(max = 20000) String content,
        @Schema(example = "[1, 3]", description = "IDs of tags owned by the current user")
        Set<@Positive Long> tagIds
) {}
