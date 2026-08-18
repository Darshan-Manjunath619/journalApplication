package com.darshan.journalApplication.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Schema(example = "darshan_01", description = "Unique username; stored lowercase")
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_]+$") String userName,
        @Schema(example = "darshan@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(example = "StrongPass123!", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password,
        @Schema(example = "false", description = "Opt in to future sentiment processing")
        boolean sentimentAnalysis
) {}
