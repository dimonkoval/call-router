CREATE SCHEMA IF NOT EXISTS callrouter;

CREATE TABLE IF NOT EXISTS callrouter.cdr (
                                              call_id VARCHAR(255) PRIMARY KEY,
                                              caller VARCHAR(255) NOT NULL,
                                              callee VARCHAR(255) NOT NULL,
                                              duration BIGINT
);
