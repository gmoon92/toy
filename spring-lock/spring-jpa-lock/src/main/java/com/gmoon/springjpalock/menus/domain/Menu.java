package com.gmoon.springjpalock.menus.domain;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Menu {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long quantity;

	@Column(nullable = false)
	private Long price;

	@Builder
	public Menu(String id, String name, Long quantity, Long price) {
		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.price = price;
	}

	public void decreaseQuantity() {
		--quantity;
	}
}
