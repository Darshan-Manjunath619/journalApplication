package com.darshan.journalApplication.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Signed JWT access token") String accessToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(example = "900", description = "Access-token lifetime in seconds") long expiresIn
) {}
