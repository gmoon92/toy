DROP TABLE IF EXISTS member CASCADE;
DROP TABLE IF EXISTS price CASCADE;
DROP TABLE IF EXISTS product CASCADE;

CREATE TABLE member
(
    id   bigint NOT NULL,
    name VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE price
(
    id       bigint NOT NULL,
    currency VARCHAR(255),
    price    DOUBLE PRECISION,
    PRIMARY KEY (id)
);

CREATE TABLE product
(
    id                   BIGINT NOT NULL,
    name                 VARCHAR(255),
    online_sale_possible BIT,
    type                 VARCHAR(255),
    PRIMARY KEY (id)
);
