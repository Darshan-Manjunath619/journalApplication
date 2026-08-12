package com.darshan.journalApplication.user.dto;

import java.util.List;

public record UserResponse(Long id, String userName, String email,
                           boolean sentimentAnalysis, List<String> roles) {}
