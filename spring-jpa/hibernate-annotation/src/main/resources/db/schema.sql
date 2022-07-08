DROP TABLE IF EXISTS member CASCADE;

CREATE TABLE member
(
    id   bigint NOT NULL,
    name VARCHAR(255),
    PRIMARY KEY (id)
);
