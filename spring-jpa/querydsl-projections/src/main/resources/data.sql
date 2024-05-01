-- user
INSERT INTO tb_user (id, name)
VALUES ('user001', '허명행');
INSERT INTO tb_user (id, name)
VALUES ('user002', '마동석');
INSERT INTO tb_user (id, name)
VALUES ('user003', '김무열');
INSERT INTO tb_user (id, name)
VALUES ('user004', '박지환');
INSERT INTO tb_user (id, name)
VALUES ('user005', '이동휘');
INSERT INTO tb_user (id, name)
VALUES ('user006', '이범수');
INSERT INTO tb_user (id, name)
VALUES ('user007', '김민재');
INSERT INTO tb_user (id, name)
VALUES ('user008', '이지훈');
INSERT INTO tb_user (id, name)
VALUES ('user009', '이주빈');

INSERT INTO tb_director (id)
VALUES ('user001');
INSERT INTO tb_actor (id)
VALUES ('user002');
INSERT INTO tb_actor (id)
VALUES ('user003');
INSERT INTO tb_actor (id)
VALUES ('user004');
INSERT INTO tb_actor (id)
VALUES ('user005');
INSERT INTO tb_actor (id)
VALUES ('user006');
INSERT INTO tb_actor (id)
VALUES ('user007');
INSERT INTO tb_actor (id)
VALUES ('user008');
INSERT INTO tb_actor (id)
VALUES ('user009');

INSERT INTO tb_user (id, name)
VALUES ('user010', '마이크 미첼');
INSERT INTO tb_user (id, name)
VALUES ('user011', '잭 블랙');
INSERT INTO tb_user (id, name)
VALUES ('user012', '아콰피나');
INSERT INTO tb_user (id, name)
VALUES ('user013', '비올라 데이비스');
INSERT INTO tb_user (id, name)
VALUES ('user014', '더스틴 호프만');
INSERT INTO tb_user (id, name)
VALUES ('user015', '제임스 홍');
INSERT INTO tb_user (id, name)
VALUES ('user016', '브라이언 크랜스톤');

INSERT INTO tb_director (id)
VALUES ('user010');
INSERT INTO tb_actor (id)
VALUES ('user011');
INSERT INTO tb_actor (id)
VALUES ('user012');
INSERT INTO tb_actor (id)
VALUES ('user013');
INSERT INTO tb_actor (id)
VALUES ('user014');
INSERT INTO tb_actor (id)
VALUES ('user015');
INSERT INTO tb_actor (id)
VALUES ('user016');

-- movie
INSERT INTO tb_movie (id, film_rating, genre, name, running_minutes,
                      release_time, release_year, release_month, release_day, release_hour)
VALUES ('movie001', 'R', 'ACTION', '범죄도시4', '109', '2024-04-24 15:00:00', 2024, 04, 23, 15);

INSERT INTO tb_movie (id, film_rating, genre, name, running_minutes,
                      release_time, release_year, release_month, release_day, release_hour)
VALUES ('movie002', 'G', 'ANIMATION', '쿵푸팬더4', '93', '2024-04-09 15:00:00', 2024, 04, 9, 15);

-- movie director
INSERT INTO tb_movie_director (id, director_id, movie_id)
VALUES ('movie-director001', 'user001', 'movie001');
INSERT INTO tb_movie_director (id, director_id, movie_id)
VALUES ('movie-director002', 'user010', 'movie002');

-- movie cast
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast001', 'user002', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast002', 'user003', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast003', 'user004', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast004', 'user005', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast005', 'user006', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast006', 'user007', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast007', 'user008', 'movie001');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast008', 'user009', 'movie001');

INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast009', 'user011', 'movie002');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast010', 'user012', 'movie002');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast011', 'user013', 'movie002');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast012', 'user014', 'movie002');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast013', 'user015', 'movie002');
INSERT INTO tb_movie_cast (id, actor_id, movie_id)
VALUES ('movie-cast014', 'user016', 'movie002');
