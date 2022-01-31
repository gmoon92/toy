DROP TABLE IF EXISTS tb_bookmark;
DROP TABLE IF EXISTS tb_cors_origin;

CREATE TABLE tb_bookmark
(
    id   bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name varchar(255),
    CONSTRAINT u_name UNIQUE (name)
);

CREATE TABLE tb_cors_origin
(
    id   bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    host     varchar(255),
    port     integer,
    `schema` varchar(255),
    CONSTRAINT u_schema_host_port UNIQUE (`schema`, host, port)
);
