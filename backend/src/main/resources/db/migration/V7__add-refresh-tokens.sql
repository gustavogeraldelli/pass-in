CREATE TABLE refresh_tokens (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    organizer_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT refresh_tokens_organizer_id_fkey
        FOREIGN KEY (organizer_id)
        REFERENCES organizers (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE UNIQUE INDEX refresh_tokens_token_hash_key ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_organizer_id ON refresh_tokens (organizer_id);
