ALTER TABLE journal_entries
    ADD COLUMN favorite BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_journal_entries_user_favorite_created_at
    ON journal_entries (user_id, favorite, created_at);
