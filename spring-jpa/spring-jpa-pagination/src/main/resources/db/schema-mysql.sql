DROP TABLE IF EXISTS lt_user_login;

CREATE TABLE lt_user_login
(
    id            varchar(50)  NOT NULL PRIMARY KEY,
    username      varchar(50),
    access_device varchar(50)  DEFAULT 'WEB',
    attempt_ip    varchar(50),
    attempt_dt    timestamp,
    succeed       bit          DEFAULT 0,
    INDEX idx_username (username)
);
