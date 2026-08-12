package com.darshan.journalApplication.auth.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 50) String userName,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
