package com.darshan.journalservice.journal.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateJournalRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 20000) String content,
        Set<@Positive Long> tagIds) {
}
