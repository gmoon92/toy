INSERT INTO tb_user (username, password, role) VALUES ('admin', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'ADMIN');
INSERT INTO tb_user (username, password, role) VALUES ('manager', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'MANAGER');
INSERT INTO tb_user (username, password, role) VALUES ('user', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'USER');

INSERT INTO tb_bookmark (name) VALUES ('gmoon92.github.io');
INSERT INTO tb_bookmark (name) VALUES ('devahea.github.io');

INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', '127.0.0.1', 8081);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', 'localhost', 8081);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('http', 'localhost', 80);
INSERT INTO tb_cors_origin (`schema`, host, port) VALUES ('https', 'gmoon92.github.io', 443);

INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (1, 'GET');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (1, 'POST');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (1, 'PUT');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (1, 'DELETE');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (0, 'HEAD');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (0, 'PATCH');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (0, 'OPTIONS');
INSERT INTO tb_access_control_allow_method (enabled, id) VALUES (0, 'TRACE');
