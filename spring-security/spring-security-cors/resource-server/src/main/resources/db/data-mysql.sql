INSERT INTO tb_user (username, password, role) VALUES ('admin', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'ADMIN');
INSERT INTO tb_user (username, password, role) VALUES ('manager', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'MANAGER');
INSERT INTO tb_user (username, password, role) VALUES ('user', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'USER');

INSERT INTO tb_bookmark (name) VALUES ('gmoon92.github.io');
INSERT INTO tb_bookmark (name) VALUES ('devahea.github.io');

INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', '127.0.0.1', 8081);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', 'localhost', 8081);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', 'localhost', 80);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('https', 'gmoon92.github.io', 443);
