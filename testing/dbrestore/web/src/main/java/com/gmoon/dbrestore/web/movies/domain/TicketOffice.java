package com.gmoon.dbrestore.web.movies.domain;

import com.gmoon.dbrestore.web.movies.domain.vo.TicketType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_ticket_office")
public class TicketOffice implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "ticketOffice")
	private Set<Movie> movies = new HashSet<>();

	public Ticket createTicket(TicketType ticketType, int sellingPrice) {
		return new Ticket(this, ticketType, new BigDecimal(sellingPrice));
	}
}
