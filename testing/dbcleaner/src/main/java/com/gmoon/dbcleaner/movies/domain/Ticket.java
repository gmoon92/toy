package com.gmoon.dbcleaner.movies.domain;

import com.gmoon.dbcleaner.movies.domain.vo.TicketType;
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
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Ticket implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ticket_office_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TicketOffice ticketOffice;

	@Column(name = "selling_price", nullable = false)
	private BigDecimal sellingPrice;

	@Column(name = "type", length = 50, nullable = false)
	@Enumerated(EnumType.STRING)
	private TicketType type;

	public Ticket(TicketOffice ticketOffice, TicketType type, BigDecimal sellingPrice) {
		this.ticketOffice = ticketOffice;
		this.sellingPrice = sellingPrice;
		this.type = type;
	}
}
