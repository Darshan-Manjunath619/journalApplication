ALTER TABLE journal_entry_tags DROP FOREIGN KEY fk_journal_entry_tags_journal;
ALTER TABLE journal_entry_tags DROP FOREIGN KEY fk_journal_entry_tags_tag;
ALTER TABLE journal_entries DROP FOREIGN KEY fk_journal_entries_user;
ALTER TABLE tags DROP FOREIGN KEY fk_tags_user;

ALTER TABLE journal_entries
    ADD CONSTRAINT fk_journal_entries_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE tags
    ADD CONSTRAINT fk_tags_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE journal_entry_tags
    ADD CONSTRAINT fk_journal_entry_tags_journal FOREIGN KEY (journal_entry_id)
        REFERENCES journal_entries (id) ON DELETE CASCADE;
ALTER TABLE journal_entry_tags
    ADD CONSTRAINT fk_journal_entry_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id) ON DELETE CASCADE;
