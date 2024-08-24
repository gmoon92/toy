package com.gmoon.dbrestore.web.movies.domain;

import java.io.Serializable;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_movie_ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovieTicket implements Serializable {

	@EmbeddedId
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Id id;

	@MapsId("movieId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "movie_id", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Movie movie;

	@MapsId("ticketId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "ticket_id", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Ticket ticket;

	public MovieTicket(Movie movie, Ticket ticket) {
		this.id = new Id(movie, ticket);
		this.movie = movie;
		this.ticket = ticket;
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@EqualsAndHashCode
	static class Id implements Serializable {

		@Column
		private Long movieId;

		@Column
		private Long ticketId;

		public Id(Movie movie, Ticket ticket) {
			this.movieId = movie.getId();
			this.ticketId = ticket.getId();
		}
	}
}
