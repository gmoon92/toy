package com.gmoon.querydslprojections.movies.ticket.domain;

import java.io.Serializable;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Ticket implements Serializable {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 50)
	private String id;

	// @ManyToMany(fetch = FetchType.LAZY)
	// @JoinTable(
	// 	name = "tb_movie_coupon",
	// 	joinColumns = @JoinColumn(name = "ticket_id", referencedColumnName = "id"),
	// 	inverseJoinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id")
	// )
	// private Set<Movie> movies;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Movie movie;

	private String userId;
}
