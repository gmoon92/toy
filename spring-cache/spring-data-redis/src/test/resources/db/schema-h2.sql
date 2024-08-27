DROP TABLE IF EXISTS tb_user;

CREATE TABLE tb_user
(
    id                      bigint        NOT NULL PRIMARY KEY,
    enabled                 bit DEFAULT 1 NOT NULL,
    email                   varchar(500)  NOT NULL,
    username                varchar(50)
);
