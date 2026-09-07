CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    date DATETIME,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    favorite BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    normalized_name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_tags_owner_normalized_name UNIQUE (owner_id, normalized_name)
);

CREATE TABLE journal_entry_tags (
    journal_entry_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (journal_entry_id, tag_id),
    CONSTRAINT fk_journal_entry_tags_journal FOREIGN KEY (journal_entry_id)
        REFERENCES journal_entries (id) ON DELETE CASCADE,
    CONSTRAINT fk_journal_entry_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id) ON DELETE CASCADE
);

CREATE INDEX idx_journal_owner_created_at
    ON journal_entries (owner_id, created_at);
CREATE INDEX idx_journal_owner_favorite_created_at
    ON journal_entries (owner_id, favorite, created_at);
CREATE INDEX idx_tags_owner_name ON tags (owner_id, normalized_name);
CREATE INDEX idx_journal_entry_tags_tag_journal
    ON journal_entry_tags (tag_id, journal_entry_id);
