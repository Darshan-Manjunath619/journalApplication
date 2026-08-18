CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    sentiment_analysis BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_users_user_name UNIQUE (user_name)
);

CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    date DATETIME,
    user_id BIGINT,
    CONSTRAINT fk_journal_entries_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_journal_entries_user_id ON journal_entries (user_id);
CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
