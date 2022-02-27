DROP TABLE IF EXISTS tb_user;
DROP TABLE IF EXISTS tb_bookmark;
DROP TABLE IF EXISTS tb_cors_origin;
DROP TABLE IF EXISTS tb_access_control_allow_method;

CREATE TABLE tb_user
(
    id                      bigint        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_non_expired     bit DEFAULT 1 NOT NULL,
    account_non_locked      bit DEFAULT 1 NOT NULL,
    credentials_non_expired bit DEFAULT 1 NOT NULL,
    enabled                 bit DEFAULT 1 NOT NULL,
    password                varchar(255),
    role                    varchar(20)   NOT NULL,
    username                varchar(255)
);

CREATE TABLE tb_bookmark
(
    id   bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name varchar(255),
    CONSTRAINT u_name UNIQUE (name)
);

CREATE TABLE tb_cors_origin
(
    id       bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    host     varchar(255),
    port     integer,
    `schema` varchar(255),
    CONSTRAINT u_schema_host_port UNIQUE (`schema`, host, port)
);

CREATE TABLE tb_access_control_allow_method
(
    id      varchar(50)   NOT NULL PRIMARY KEY,
    enabled bit DEFAULT 1 NOT NULL
);
