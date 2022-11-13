INSERT INTO tb_book (id, name, isbn, price, publication_date) VALUES ('book-no0', 'START! DDD', '111-111-111-100', 25200, '2016-05-27T01:05:46.560');
INSERT INTO tb_book (id, name, isbn, price, publication_date) VALUES ('book-no1', 'Clean Code', '111-111-111-101', 29700, '2013-12-24T01:05:46.560');
INSERT INTO tb_book (id, name, isbn, price, publication_date) VALUES ('book-no2', 'TDD', '111-111-111-102', 27000, '2014-02-15T01:05:46.560');
INSERT INTO tb_book (id, name, isbn, price, publication_date) VALUES ('book-no3', 'Clean Architecture', '111-111-111-103', 26100, '2019-08-20T01:05:46.560');

INSERT INTO tb_bookstor (id, name) VALUES ('book-store-1', 'gmoons');

INSERT INTO tb_bookstor_book (id, book_store_id, book_id, book_name, type, status, quantity, created_date, modified_date) VALUES ('book-store-book-0', 'book-store-1', 'book-no0', 'START! DDD', 'ETC', 'DISPLAY', 10, '2022-10-01T01:05:46.560', '2022-10-01T01:05:46.560');
INSERT INTO tb_bookstor_book (id, book_store_id, book_id, book_name, type, status, quantity, created_date, modified_date) VALUES ('book-store-book-1', 'book-store-1', 'book-no1', 'Clean Code', 'ETC', 'DISPLAY', 10, '2022-10-02T01:05:46.560', '2022-10-02T01:05:46.560');
INSERT INTO tb_bookstor_book (id, book_store_id, book_id, book_name, type, status, quantity, created_date, modified_date) VALUES ('book-store-book-2', 'book-store-1', 'book-no2', 'TDD', 'ETC', 'HIDDEN', 0, '2022-10-03T01:05:46.560', '2022-10-03T01:05:46.560');
INSERT INTO tb_bookstor_book (id, book_store_id, book_id, book_name, type, status, quantity, created_date, modified_date) VALUES ('book-store-book-3', 'book-store-1', 'book-no3', 'Clean Architecture', 'ETC', 'DISPLAY', 1, '2022-10-04T01:05:46.560', '2022-10-04T01:05:46.560');
