CREATE DATABASE IF NOT EXISTS batchinsert;

use batchinsert;

DROP TABLE IF EXISTS tb_access_log;
CREATE TABLE tb_access_log (
                           id varchar(50) NOT NULL,
                           username varchar(50) NOT NULL,
                           ip varchar(100) NOT NULL,
                           os varchar(30) NOT NULL,
                           attempt_dt datetime NOT NULL,
                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
