package com.darshan.journalApplication.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {}
