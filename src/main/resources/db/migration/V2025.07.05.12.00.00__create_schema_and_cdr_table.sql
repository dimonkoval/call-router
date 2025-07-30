CREATE SCHEMA IF NOT EXISTS callrouter;

CREATE TABLE IF NOT EXISTS callrouter.cdr (
                                              id SERIAL PRIMARY KEY,
                                              call_id VARCHAR(255) NOT NULL,
                                              from_number VARCHAR(255) NOT NULL,
                                              to_number VARCHAR(255) NOT NULL,
                                              start_time BIGINT,
                                              end_time BIGINT,
                                              duration BIGINT NOT NULL,
                                              from_contact_name VARCHAR(255),
                                              to_contact_name VARCHAR(255),
                                              status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cdr_call_id ON callrouter.cdr(call_id);

ALTER TABLE callrouter.cdr
    ADD CONSTRAINT uq_cdr_call_id UNIQUE (call_id);