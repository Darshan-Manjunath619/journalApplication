package com.darshan.journalApplication.journal;

import java.time.Instant;

public record JournalSearchCriteria(String query, Instant from, Instant to, Boolean favorite) {
}
