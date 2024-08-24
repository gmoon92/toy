package com.gmoon.dbrestore.web.movies.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.gmoon.dbrestore.web.movies.domain.vo.TicketType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ticket_office_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TicketOffice ticketOffice;

	@Column(nullable = false)
	private BigDecimal sellingPrice;

	@Column(length = 50, nullable = false)
	@Enumerated(EnumType.STRING)
	private TicketType type;

	public Ticket(TicketOffice ticketOffice, TicketType type, BigDecimal sellingPrice) {
		this.ticketOffice = ticketOffice;
		this.sellingPrice = sellingPrice;
		this.type = type;
	}
}
