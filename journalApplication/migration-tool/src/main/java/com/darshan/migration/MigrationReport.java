package com.darshan.migration;

public record MigrationReport(String mode, long journals, long tags, long links,
                              long orphanedJournalOwners, long orphanedTagOwners) {
    public String format() {
        return ("Mode: %s%nJournals: %d%nTags: %d%nJournal-tag links: %d%n" +
                "Orphaned journal owners: %d%nOrphaned tag owners: %d%nResult: PASSED%n")
                .formatted(mode, journals, tags, links,
                        orphanedJournalOwners, orphanedTagOwners);
    }
}
