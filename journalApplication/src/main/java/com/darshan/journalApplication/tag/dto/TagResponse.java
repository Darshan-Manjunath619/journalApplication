package com.darshan.journalApplication.tag.dto;

import java.time.Instant;

public record TagResponse(Long id, String name, Instant createdAt) {
}
