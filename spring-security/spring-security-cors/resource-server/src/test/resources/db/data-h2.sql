DELETE FROM "tb_user";
DELETE FROM "tb_bookmark";
DELETE FROM "tb_cors_origin";
DELETE FROM "tb_access_control_allow_method";

INSERT INTO "tb_user" ("id", "username", "password", "role") VALUES (1, 'admin', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'ADMIN');
INSERT INTO "tb_user" ("id", "username", "password", "role") VALUES (2, 'manager', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'MANAGER');
INSERT INTO "tb_user" ("id", "username", "password", "role") VALUES (3, 'user', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu', 'USER');

INSERT INTO "tb_bookmark" ("id", "name") VALUES (1, 'gmoon92.github.io');
INSERT INTO "tb_bookmark" ("id", "name") VALUES (2, 'devahea.github.io');

INSERT INTO "tb_cors_origin" ("id", "schema", "host", "port") VALUES (1, 'http', '127.0.0.1', 8081);
INSERT INTO "tb_cors_origin" ("id", "schema", "host", "port") VALUES (2, 'http', 'localhost', 8081);
INSERT INTO "tb_cors_origin" ("id", "schema", "host", "port") VALUES (3, 'http', 'localhost', 80);
INSERT INTO "tb_cors_origin" ("id", "schema", "host", "port") VALUES (4, 'https', 'gmoon92.github.io', 443);

INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (1, 'GET');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (1, 'POST');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (1, 'PUT');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (1, 'DELETE');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (0, 'HEAD');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (0, 'PATCH');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (0, 'OPTIONS');
INSERT INTO "tb_access_control_allow_method" ("enabled", "id") VALUES (0, 'TRACE');
