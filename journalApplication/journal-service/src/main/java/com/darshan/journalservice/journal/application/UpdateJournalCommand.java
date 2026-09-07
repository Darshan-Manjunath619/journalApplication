package com.darshan.journalservice.journal.application;

import java.util.Set;

public record UpdateJournalCommand(String title, String content,
                                   Boolean favorite, Set<Long> tagIds) {
}
