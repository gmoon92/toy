# QueryDSL Projections

## DDL

```sql
[QUERY] drop table if exists coupon
[QUERY] drop table if exists movie
[QUERY] drop table if exists tb_movie_coupon
    
[QUERY] create table coupon (id varchar(50) not null, expire_dt datetime(6), primary key (id))
[QUERY] create table movie (id varchar(50) not null, name varchar(255), primary key (id))
    
[QUERY] create table tb_movie_coupon (coupon_id varchar(50) not null, movie_id varchar(50) not null, primary key (coupon_id, movie_id))
[QUERY] alter table tb_movie_coupon add constraint FKpthc7dl2xg4a4y8jr9fj9k795 foreign key (movie_id) references movie (id)
[QUERY] alter table tb_movie_coupon add constraint FKm5l9copyc29tj5yq8pkgd082n foreign key (coupon_id) references coupon (id) on delete cascade
        
[QUERY] SET autocommit=0
```

