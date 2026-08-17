package com.darshan.journalApplication.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email @Size(min = 5, max = 254) String email,
        Boolean sentimentAnalysis
) {
    @AssertTrue(message = "At least one profile field must be provided")
    public boolean hasChanges() {
        return email != null || sentimentAnalysis != null;
    }
}
