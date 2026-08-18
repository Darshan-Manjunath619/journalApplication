CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    normalized_name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_tags_user_normalized_name UNIQUE (user_id, normalized_name)
);

CREATE TABLE journal_entry_tags (
    journal_entry_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (journal_entry_id, tag_id),
    CONSTRAINT fk_journal_entry_tags_journal FOREIGN KEY (journal_entry_id)
        REFERENCES journal_entries (id),
    CONSTRAINT fk_journal_entry_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE INDEX idx_tags_user_name ON tags (user_id, normalized_name);
CREATE INDEX idx_journal_entry_tags_tag_journal
    ON journal_entry_tags (tag_id, journal_entry_id);
