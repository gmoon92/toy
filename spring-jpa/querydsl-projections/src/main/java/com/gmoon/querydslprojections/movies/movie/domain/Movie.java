package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.querydslprojections.movies.users.user.domain.Actor;
import com.gmoon.querydslprojections.movies.users.user.domain.Director;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	 name = "tb_movie",
	 indexes = {
		  @Index(name = "idx_name", columnList = "name,genre"),
		  @Index(name = "idx_release_time", columnList = "release_year,release_month,release_day,release_hour")
	 }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movie implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	@EqualsAndHashCode.Include
	private String id;

	@Column(length = 100, nullable = false)
	private String name;

	@OneToOne(mappedBy = "movie", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	private MovieDirector director;

	@Builder.Default
	@OneToMany(mappedBy = "movie", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Set<MovieCast> castMembers = new HashSet<>();

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private MovieGenre genre = MovieGenre.ACTION;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private FilmRating filmRating = FilmRating.G;

	@Column
	private long runningMinutes;

	@Embedded
	@AttributeOverrides({
		 @AttributeOverride(name = "value", column = @Column(name = "release_time", nullable = false)),
		 @AttributeOverride(name = "year", column = @Column(name = "release_year", nullable = false)),
		 @AttributeOverride(name = "month", column = @Column(name = "release_month", nullable = false)),
		 @AttributeOverride(name = "dayOfMonth", column = @Column(name = "release_day", nullable = false)),
		 @AttributeOverride(name = "hour", column = @Column(name = "release_hour", nullable = false))
	})
	private MovieReleaseTime releaseTime;

	public Movie chooseDirector(Director director) {
		this.director = MovieDirector.create(this, director);
		return this;
	}

	public Movie castingActor(Actor actor) {
		castMembers.add(MovieCast.create(this, actor));
		return this;
	}

	public String getDirectorName() {
		return director.getName();
	}
}
