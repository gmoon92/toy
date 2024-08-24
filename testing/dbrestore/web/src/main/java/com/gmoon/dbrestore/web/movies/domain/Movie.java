package com.gmoon.dbrestore.web.movies.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_movie")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movie implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(length = 30)
	private String title;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ticket_office_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TicketOffice ticketOffice;

	//	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
//	@JoinTable(
//		 name = "tb_movie_ticket",
//		 joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"),
//		 inverseJoinColumns = @JoinColumn(name = "ticket_id", referencedColumnName = "id")
//	)
//	private Set<Ticket> tickets = new HashSet<>();
	@OneToMany(mappedBy = "movie", cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	private Set<MovieTicket> tickets = new HashSet<>();

	@Builder
	public Movie(Long id, String title, TicketOffice ticketOffice, Set<MovieTicket> tickets) {
		this.id = id;
		this.title = title;
		this.ticketOffice = ticketOffice;
		this.tickets = tickets;
	}

	public void addTicket(Ticket ticket) {
		tickets.add(new MovieTicket(this, ticket));
	}
}
