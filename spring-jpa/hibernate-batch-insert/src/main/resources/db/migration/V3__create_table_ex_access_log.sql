CREATE DATABASE IF NOT EXISTS batchinsert;

use batchinsert;

DROP TABLE IF EXISTS ex_access_log;
CREATE TABLE ex_access_log (
                               id varchar(50) NOT NULL,
                               username varchar(50) NOT NULL,
                               ip varchar(100) NOT NULL,
                               os varchar(30) NOT NULL,
                               attempt_dt varchar(50) NOT NULL,
                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
