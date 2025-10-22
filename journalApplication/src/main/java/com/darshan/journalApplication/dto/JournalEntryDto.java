package com.darshan.journalApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntryDto {
    @NonNull
    private String title;

    private String content;
}
