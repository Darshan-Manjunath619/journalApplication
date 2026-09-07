package com.darshan.journalservice.tag.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(@NotBlank @Size(max = 80) String name) {
}
