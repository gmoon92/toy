package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;

import com.gmoon.querydslprojections.movies.users.user.domain.Actor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_movie_cast")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class MovieCast implements Serializable {

	@EmbeddedId
	private Id id = new Id();

	@ManyToOne(optional = false)
	@JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
	private Movie movie;

	@OneToOne(optional = false)
	@JoinColumn(name = "actor_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
	private Actor actor;

	protected static MovieCast create(Movie movie, Actor actor) {
		MovieCast movieCast = new MovieCast();
		movieCast.id = new Id(movie, actor);
		movieCast.movie = movie;
		movieCast.actor = actor;
		return movieCast;
	}

	public String getName() {
		return actor.getName();
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	static class Id implements Serializable {

		@Column(name = "movie_id", length = 50, nullable = false)
		private String movieId;

		@Column(name = "actor_id", length = 50, nullable = false)
		private String actorId;

		protected Id(Movie movie, Actor actor) {
			movieId = movie.getId();
			actorId = actor.getId();
		}
	}
}
