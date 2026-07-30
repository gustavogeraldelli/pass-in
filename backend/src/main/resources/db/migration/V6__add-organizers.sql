CREATE TABLE organizers (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX organizers_email_key ON organizers (email);

ALTER TABLE events ADD COLUMN organizer_id VARCHAR(255);

ALTER TABLE events
    ADD CONSTRAINT events_organizer_id_fkey
    FOREIGN KEY (organizer_id)
    REFERENCES organizers (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

CREATE INDEX idx_events_organizer_id ON events (organizer_id);
