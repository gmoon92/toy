package com.gmoon.dbcleaner.movies.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_movie")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = "id", exclude = "tickets")
public class Movie implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	private String title;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ticket_office_id", referencedColumnName = "id")
	private TicketOffice ticketOffice;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinTable(
		 name = "tb_movie_ticket",
		 joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"),
		 inverseJoinColumns = @JoinColumn(name = "ticket_id", referencedColumnName = "id")
	)
	private Set<Ticket> tickets = new HashSet<>();
}
