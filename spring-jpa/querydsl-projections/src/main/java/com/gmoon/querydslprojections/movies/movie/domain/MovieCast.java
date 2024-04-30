package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gmoon.querydslprojections.movies.users.actor.domain.Actor;

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

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "movie_id", referencedColumnName = "id")
	private Movie movie;

	@OneToOne(optional = false)
	@JoinColumn(name = "actor_id", referencedColumnName = "id", nullable = false)
	private Actor actor;

}
