INSERT INTO "user" ("id", "name", "email") VALUES ('user0', 'gmoon', 'gmoon0929@gmail.com');

INSERT INTO "cart" ("id", "user_id", "product_no") VALUES ('cart0', 'user0', 'product0');
INSERT INTO "cart" ("id", "user_id", "product_no") VALUES ('cart1', 'user0', 'product1');

INSERT INTO "order" ("order_no", "status", "user_id", "user_name", "user_email") VALUES ('order0', 'ACCEPTED', 'user0', 'gmoon', 'gmoon92@gmail.com');

INSERT INTO "order_line_item" ("id", "order_no", "product_no", "product_name", "product_price", "quantity") VALUES ('item0', 'order0', 'product0', 'Start! DDD', 36000, 1);
INSERT INTO "order_line_item" ("id", "order_no", "product_no", "product_name", "product_price", "quantity") VALUES ('item1', 'order0', 'product1', 'Clean Code', 16000, 2);
