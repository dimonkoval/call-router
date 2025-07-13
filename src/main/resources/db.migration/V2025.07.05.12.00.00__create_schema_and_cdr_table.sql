CREATE SCHEMA IF NOT EXISTS callrouter;

CREATE TABLE IF NOT EXISTS callrouter.cdr (
                                              id SERIAL PRIMARY KEY,
                                              call_id VARCHAR(255) NOT NULL,
                                              from_number VARCHAR(255) NOT NULL,
                                              to_number VARCHAR(255) NOT NULL,
                                              start_time BIGINT NOT NULL,
                                              end_time BIGINT NOT NULL,
                                              duration BIGINT NOT NULL,
                                              status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_cdr_call_id ON callrouter.cdr(call_id);

ALTER TABLE callrouter.cdr
    ADD CONSTRAINT uq_cdr_call_id UNIQUE (call_id);