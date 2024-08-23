package com.gmoon.querydslprojections.movies.movie.infra;

import static com.gmoon.querydslprojections.movies.movie.domain.QMovie.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.querydslprojections.global.Fixtures;
import com.gmoon.querydslprojections.global.JpaConfig;
import com.gmoon.querydslprojections.movies.movie.domain.FilmRating;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;
import com.gmoon.querydslprojections.movies.movie.dto.MovieResponse;
import com.gmoon.querydslprojections.movies.movie.dto.QMovieResponse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DataJpaTest(
	 includeFilters = @ComponentScan.Filter(
		  type = FilterType.ASSIGNABLE_TYPE, classes = {
		  JpaConfig.class,
		  MovieRepositoryAdapter.class
	 })
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryProjectionsTest {

	@Autowired
	private JPAQueryFactory queryFactory;

	/**
	 * protected
	 * <pre>
	 * Caused by: java.lang.IllegalAccessException: Class com.querydsl.core.types.QBean can not access a member of class com.gmoon.querydslprojections.movies.movie.domain.Movie with modifiers "protected"
	 * 	at sun.reflect.Reflection.ensureMemberAccess(Reflection.java:102)
	 * 	at java.lang.Class.newInstance(Class.java:436)
	 * 	at com.querydsl.core.types.QBean.create(QBean.java:251)
	 * 	at com.querydsl.core.types.QBean.newInstance(QBean.java:222)
	 * 	</pre>
	 * */
	@DisplayName("bean setter prop. Constructor and setter props must be public modifiers.")
	@Test
	void bean() {
		MovieDTO result = queryFactory
			 .select(
				  Projections.bean(
					   MovieDTO.class,
					   movie.id,
					   // movie.name, // 타깃 객체에 setName 필요.
					   Expressions.as(movie.name, "movieName"),
					   movie.genre
				  )
			 )
			 .from(movie)
			 .where(movie.id.eq(Fixtures.MOVIE_ID_001))
			 .fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@DisplayName("fields. Constructor must be public modifiers.")
	@Test
	void fields() {
		MovieDTO result = queryFactory
			 .select(
				  Projections.fields(
					   MovieDTO.class,
					   movie.id,
					   Expressions.as(movie.name, "movieName"),
					   movie.genre
				  )
			 )
			 .from(movie)
			 .where(movie.id.eq(Fixtures.MOVIE_ID_001))
			 .fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@DisplayName("constructor. Constructor must be public modifiers.")
	@Test
	void constructor() {
		MovieDTO result = queryFactory
			 .select(
				  Projections.constructor(
					   MovieDTO.class,
					   movie.id,
					   movie.name,
					   movie.genre
				  )
			 )
			 .from(movie)
			 .where(movie.id.eq(Fixtures.MOVIE_ID_001))
			 .fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@Test
	void queryProjectionsAnnotation() {
		MovieResponse response = queryFactory
			 .select(
				  new QMovieResponse(
					   movie.id,
					   movie.name,
					   movie.genre,
					   movie.filmRating,
					   movie.director.director.name,
					   movie.releaseTime
				  )
			 )
			 .from(movie)
			 .where(movie.id.eq(Fixtures.MOVIE_ID_001))
			 .fetchFirst();

		assertThat(response.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(response.getMovieName()).isEqualTo("범죄도시4");
		assertThat(response.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@NoArgsConstructor(access = AccessLevel.PUBLIC)
	@Getter
	@Setter(AccessLevel.PUBLIC)
	public static class MovieDTO {

		private String id;
		private String movieName;
		private MovieGenre genre;
		private FilmRating filmRating;
		private String directorName;
		private List<String> castNames;
		private Long releaseDateTime;

		public MovieDTO(String id, String movieName, MovieGenre genre) {
			this.id = id;
			this.movieName = movieName;
			this.genre = genre;
		}
	}
}
