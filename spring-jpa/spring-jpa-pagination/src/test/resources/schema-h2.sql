DROP TABLE IF EXISTS lt_user_login;

CREATE TABLE lt_user_login
(
    id            varchar(50)  NOT NULL PRIMARY KEY,
    username      varchar(50),
    access_device varchar(50)  DEFAULT 'WEB',
    attempt_ip    varchar(50),
    attempt_dt    timestamp,
    succeed       boolean      DEFAULT 0
);

CREATE INDEX idx_username ON lt_user_login(username);
CREATE INDEX idx_attempt_dt ON lt_user_login(attempt_dt);
