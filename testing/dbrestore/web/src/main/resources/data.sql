INSERT INTO tb_ticket_office (id, name) VALUES (1, 'gmoon');
INSERT INTO tb_movie (id, title, ticket_office_id) VALUES (1, 'movie1', 1);
INSERT INTO tb_movie (id, title, ticket_office_id) VALUES (2, 'movie2', 1);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (1, 1, 'NORMAL', 3000);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (2, 1, 'NORMAL', 3000);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (3, 1, 'NORMAL', 3000);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (4, 1, 'NORMAL', 3000);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (5, 1, 'NORMAL', 3000);
INSERT INTO tb_ticket (id, ticket_office_id, type, selling_price)
VALUES (6, 1, 'NORMAL', 3000);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (1, 1);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (1, 2);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (1, 3);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (1, 4);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (1, 5);
INSERT INTO tb_movie_ticket (movie_id, ticket_id) VALUES (2, 6);
INSERT INTO tb_coupon (id, movie_id, used)
VALUES (1, 1, false);
INSERT INTO tb_coupon (id, movie_id, used)
VALUES (2, 1, false);
INSERT INTO tb_coupon (id, movie_id, used)
VALUES (3, 1, false);
INSERT INTO tb_coupon (id, movie_id, used)
VALUES (4, 1, false);
INSERT INTO tb_coupon (id, movie_id, used)
VALUES (5, 1, false);
