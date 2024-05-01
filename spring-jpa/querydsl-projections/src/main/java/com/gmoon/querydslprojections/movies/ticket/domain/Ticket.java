package com.gmoon.querydslprojections.movies.ticket.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_ticket")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Ticket implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
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
