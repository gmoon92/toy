DROP TABLE IF EXISTS lt_user_login;

CREATE TABLE lt_user_login
(
    id            varchar(50)  NOT NULL PRIMARY KEY,
    username      varchar(50),
    access_device varchar(50)  DEFAULT 'WEB',
    attempt_ip    varchar(50),
    attempt_at timestamp,
    succeed       boolean      DEFAULT 0
);

CREATE INDEX idx_username ON lt_user_login(username);
CREATE INDEX idx_attempt_at ON lt_user_login (attempt_at);


DROP TABLE IF EXISTS tb_user_group;

CREATE TABLE tb_user_group
(
    id                  varchar(50)  NOT NULL PRIMARY KEY,
    name                varchar(50),
    parent_id           varchar(50) null,
    CONSTRAINT FK_parent_group_id
        FOREIGN KEY (parent_id) REFERENCES tb_user_group(id)
);

DROP TABLE IF EXISTS tb_user;

CREATE TABLE tb_user
(
    id            varchar(50)  NOT NULL PRIMARY KEY,
    username      varchar(20),
    user_group_id varchar(50) not null,
    CONSTRAINT FK_user_group_id
        FOREIGN KEY (user_group_id) REFERENCES tb_user_group(id)
);
