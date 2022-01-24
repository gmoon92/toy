INSERT INTO tb_team (id, name) VALUES (0, 'web1');
INSERT INTO tb_user (id, role, username, password) VALUES (0, 'ADMIN', 'admin', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu');
INSERT INTO tb_user (id, role, username, password) VALUES (1, 'MANAGER', 'manager', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu');
INSERT INTO tb_user (id, role, username, password) VALUES (2, 'USER', 'user1', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu');
INSERT INTO tb_user (id, role, username, password) VALUES (3, 'USER', 'user2', '{bcrypt}$2a$10$yt2xpbUPeuX6zMuaScPLr.J5n9rBnhtybFlrK/RaQqI9Qh.JwAhyu');
INSERT INTO tb_team_user (team_id, user_id) VALUES (0, 0);
INSERT INTO tb_team_user (team_id, user_id) VALUES (0, 1);
INSERT INTO tb_team_user (team_id, user_id) VALUES (0, 2);
INSERT INTO tb_team_user (team_id, user_id) VALUES (0, 3);
