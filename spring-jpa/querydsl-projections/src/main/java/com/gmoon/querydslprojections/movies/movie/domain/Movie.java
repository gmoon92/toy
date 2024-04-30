package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "tb_movie",
	indexes = {
		@Index(name = "idx_name", columnList = "name,genre"),
		@Index(name = "idx_release_time", columnList = "release_year,release_month,release_day,release_hour")
	}
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Movie implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@OneToOne(mappedBy = "movie", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	private MovieDirector director;

	@OneToMany(mappedBy = "movie", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private Set<MovieCast> castMembers;

	@Enumerated(EnumType.STRING)
	@Column(name = "genre", length = 50, nullable = false)
	private MovieGenre genre;

	@Enumerated(EnumType.STRING)
	@Column(name = "film_rating", length = 50, nullable = false)
	private FilmRatings filmRatings;

	@Column(name = "running_minutes")
	private long runningMinutes;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "releaseTime", column = @Column(name = "release_time", nullable = false)),
		@AttributeOverride(name = "year", column = @Column(name = "release_year", nullable = false)),
		@AttributeOverride(name = "month", column = @Column(name = "release_month", nullable = false)),
		@AttributeOverride(name = "dayOfMonth", column = @Column(name = "release_day", nullable = false)),
		@AttributeOverride(name = "hour", column = @Column(name = "release_hour", nullable = false))
	})
	private MovieReleaseTime releaseTime;
}
