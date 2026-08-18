package com.darshan.journalApplication.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @Schema(example = "Spring", description = "Unique per user, ignoring case and surrounding spaces")
        @NotBlank @Size(max = 80) String name) {
}
