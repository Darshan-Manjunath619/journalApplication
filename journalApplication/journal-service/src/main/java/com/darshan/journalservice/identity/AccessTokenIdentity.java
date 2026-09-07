package com.darshan.journalservice.identity;

import java.util.List;

public record AccessTokenIdentity(Long userId, String username, List<String> roles) {
    public AccessTokenIdentity {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Access-token user ID must be positive");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Access-token username is required");
        }
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
