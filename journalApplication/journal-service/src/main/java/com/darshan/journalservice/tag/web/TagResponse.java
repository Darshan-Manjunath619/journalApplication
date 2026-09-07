package com.darshan.journalservice.tag.web;

import java.time.Instant;

public record TagResponse(Long id, String name, Instant createdAt) {
}
