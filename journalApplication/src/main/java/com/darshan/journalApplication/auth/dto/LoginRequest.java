package com.darshan.journalApplication.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @Schema(example = "darshan_01")
        @NotBlank @Size(min = 3, max = 50) String userName,
        @Schema(example = "StrongPass123!", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password
) {}
