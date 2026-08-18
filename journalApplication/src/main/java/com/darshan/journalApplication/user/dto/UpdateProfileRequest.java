package com.darshan.journalApplication.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Schema(example = "new-email@example.com")
        @Email @Size(min = 5, max = 254) String email,
        @Schema(example = "true")
        Boolean sentimentAnalysis
) {
    @AssertTrue(message = "At least one profile field must be provided")
    public boolean hasChanges() {
        return email != null || sentimentAnalysis != null;
    }
}
