package com.gmoon.querydslprojections.movies.movie.infra;

import static com.gmoon.querydslprojections.movies.movie.domain.QMovie.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.querydslprojections.global.Fixtures;
import com.gmoon.querydslprojections.global.JpaConfig;
import com.gmoon.querydslprojections.movies.movie.domain.FilmRatings;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;

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
		MovieResponse response = queryFactory
			.select(
				Projections.bean(
					MovieResponse.class,
					movie.id,
					movie.name,
					movie.genre
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(response.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(response.getMovieName()).isEqualTo("범죄도시4");
		assertThat(response.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@DisplayName("fields. Constructor and setter props must be public modifiers.")
	@Test
	void fields() {
		MovieResponse response = queryFactory
			.select(
				Projections.fields(
					MovieResponse.class,
					movie.id,
					Expressions.as(movie.name, "movieName"),
					movie.genre
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(response.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(response.getMovieName()).isEqualTo("범죄도시4");
		assertThat(response.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@DisplayName("constructor. Constructor and setter props must be public modifiers.")
	@Test
	void constructor() {
		MovieResponse response = queryFactory
			.select(
				Projections.constructor(
					MovieResponse.class,
					movie.id,
					Expressions.as(movie.name, "movieName"),
					movie.genre
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
	public static class MovieResponse {

		private String id;
		private String movieName;
		private MovieGenre genre;
		private FilmRatings filmRatings;
		private String directorName;
		private List<String> castNames;
		private Long releaseDateTime;

		public MovieResponse(String id, String movieName, MovieGenre genre2) {
			this.id = id;
			this.movieName = movieName;
			this.genre = genre2;
		}

		@QueryProjection
		public MovieResponse(String id, String movieName, MovieGenre genre, FilmRatings filmRatings,
			String directorName,
			List<String> castNames,
			LocalDateTime releaseDateTime) {
			this.id = id;
			this.movieName = movieName;
			this.genre = genre;
			this.filmRatings = filmRatings;
			this.directorName = directorName;
			this.castNames = castNames;
			this.releaseDateTime = releaseDateTime.toEpochSecond(ZoneOffset.UTC);
		}

		public void setName(String movieName) {
			this.movieName = movieName;
		}
	}
}
