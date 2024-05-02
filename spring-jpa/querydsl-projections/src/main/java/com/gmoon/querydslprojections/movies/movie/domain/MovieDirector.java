package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.gmoon.querydslprojections.movies.users.user.domain.Director;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_movie_director")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class MovieDirector implements Serializable {

	@EmbeddedId
	private Id id = new Id();

	@OneToOne(optional = false)
	@JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
	private Movie movie;

	@OneToOne(optional = false)
	@JoinColumn(name = "director_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
	private Director director;

	protected static MovieDirector create(final Movie movie, final Director director) {
		MovieDirector movieDirector = new MovieDirector();
		movieDirector.id = new Id(movie, director);
		movieDirector.director = director;
		return movieDirector;
	}

	public String getName() {
		return director.getName();
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	static class Id implements Serializable {

		@Column(name = "movie_id", length = 50, nullable = false)
		private String movieId;

		@Column(name = "director_id", length = 50, nullable = false)
		private String directorId;

		protected Id(Movie movie, Director director) {
			this.movieId = movie.getId();
			this.directorId = director.getId();
		}
	}
}
