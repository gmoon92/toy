CREATE DATABASE IF NOT EXISTS batchinsert;

use batchinsert;

DROP TABLE IF EXISTS tb_coupon_group;
CREATE TABLE tb_coupon_group (
                                 id VARCHAR(50) NOT NULL,
                                 name VARCHAR(20) NOT NULL,
                                 coupon_count INT NOT NULL,
                                 start_at DATETIME NOT NULL,
                                 end_at DATETIME NOT NULL,
                                 created_at DATETIME NOT NULL,
                                 PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE tb_coupon (
                           no VARCHAR(50) NOT NULL,
                           coupon_group_id VARCHAR(50) NOT NULL,
                           start_at DATETIME NOT NULL,
                           end_at DATETIME NOT NULL,
                           created_at DATETIME NOT NULL,
                           registered_by VARCHAR(50) NULL,
                           registered_at DATETIME    NULL,
                           PRIMARY KEY (no),
                           CONSTRAINT fk_coupon_coupon_group_id
                               FOREIGN KEY (coupon_group_id)
                                   REFERENCES tb_coupon_group (id)
                                   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
