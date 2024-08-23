package com.gmoon.springjpalock.orders.domain;

import java.io.Serializable;

import com.gmoon.springjpalock.menus.domain.Menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tb_order_line_item")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderLineItem implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 50)
	private String id;

	@Setter
	@ManyToOne(optional = false)
	@JoinColumn(name = "menu_id", referencedColumnName = "id")
	private Menu menu;

	@Column(name = "quantity", nullable = false)
	private long quantity;

	@Builder
	private OrderLineItem(Menu menu, long quantity) {
		this.menu = menu;
		this.quantity = quantity;
	}
}
