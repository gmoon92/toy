INSERT INTO tb_book (id, name, isbn, price) VALUES ('book-no0', 'START! DDD', '111-111-111-100', 36000);
INSERT INTO tb_book (id, name, isbn, price) VALUES ('book-no1', 'Clean Code', '111-111-111-101', 36000);
INSERT INTO tb_book (id, name, isbn, price) VALUES ('book-no2', 'TDD', '111-111-111-102', 36000);
INSERT INTO tb_book (id, name, isbn, price) VALUES ('book-no3', 'Clean Architecture', '111-111-111-103', 36000);

INSERT INTO tb_bookstor (id, name) VALUES ('book-store-1', 'gmoons');

INSERT INTO tb_bookstor_book (id, book_id, type, status, quantity) VALUES (0, 'book-no0', 'ETC', 'DISPLAY', 10);
INSERT INTO tb_bookstor_book (id, book_id, type, status, quantity) VALUES (1, 'book-no1', 'ETC', 'DISPLAY', 10);
INSERT INTO tb_bookstor_book (id, book_id, type, status, quantity) VALUES (2, 'book-no2', 'ETC', 'HIDDEN', 0);
INSERT INTO tb_bookstor_book (id, book_id, type, status, quantity) VALUES (3, 'book-no3', 'ETC', 'DISPLAY', 1);
