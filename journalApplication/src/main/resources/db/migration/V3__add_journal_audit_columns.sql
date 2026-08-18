ALTER TABLE journal_entries
    ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE journal_entries
    ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

CREATE INDEX idx_journal_entries_user_created_at
    ON journal_entries (user_id, created_at);
