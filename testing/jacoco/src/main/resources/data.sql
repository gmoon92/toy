INSERT INTO tb_user (id, role, username, password, enabled) VALUES ('admin', 'ADMIN', 'gmoon0929@gmail.com', '{bcrypt}$2a$10$SjCgNeUlAdoAeOHsIjUXROJQmSRjxRVEzBOcv26.s1UR71nJT2C6S', true);
INSERT INTO tb_user_option (user_id, allows_receiving_mail) VALUES ('admin', true);
