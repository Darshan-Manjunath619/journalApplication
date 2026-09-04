package com.darshan.journalApplication.journal.port;

/** Journal-owned contract for resolving a username to its stable owner ID. */
public interface JournalOwnerIdentityPort {
    Long requireOwnerId(String userName);
}
