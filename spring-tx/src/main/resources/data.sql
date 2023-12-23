INSERT INTO tb_space (id, name) VALUES ('space01', 'root');
INSERT INTO tb_space_user (id, user_id, space_id) VALUES ('spaceuser01', 'user01', 'space01');
INSERT INTO tb_favorite (user_id, type) VALUES ('user01', 'SPACE_USER');
INSERT INTO tb_favorite (user_id, type) VALUES ('user02', 'SPACE_USER');
INSERT INTO tb_favorite_user (id, fuser_id, ftype, user_id) VALUES ('favorite_user01', 'user01', 'SPACE_USER', 'user02');
INSERT INTO tb_favorite_user (id, fuser_id, ftype, user_id) VALUES ('favorite_user02', 'user01', 'SPACE_USER', 'user03');
INSERT INTO tb_favorite_user (id, fuser_id, ftype, user_id) VALUES ('favorite_user03', 'user02', 'SPACE_USER', 'user01');
