package com.darshan.journalservice.journal.application;

import java.time.Instant;

public record JournalSearchCriteria(String query, Instant from, Instant to,
                                    Boolean favorite, Long tagId) {
}
