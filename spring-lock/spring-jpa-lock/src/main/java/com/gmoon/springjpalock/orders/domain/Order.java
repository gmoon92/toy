package com.gmoon.springjpalock.orders.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springjpalock.orders.domain.vo.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tb_order")
@Entity
@OptimisticLocking(type = OptimisticLockType.VERSION)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String no;

	@Column(length = 50)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private OrderStatus status;

	@Version
	@Setter
	private Long version;

	@ColumnDefault("0")
	@OptimisticLock(excluded = true)
	@Column
	private Long issuedCount;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "order_no")
	private List<OrderLineItem> orderLineItems = new ArrayList<>();

	@Builder
	private Order(String no, String address, OrderStatus status, Long version, Long issuedCount,
		 List<OrderLineItem> orderLineItems) {
		this.no = no;
		this.address = address;
		this.status = status;
		this.version = version;
		this.issuedCount = issuedCount;
		this.orderLineItems = orderLineItems;
	}

	public Order(String address) {
		this.address = address;
		this.status = OrderStatus.WAITING;
	}

	public void changeAddress(String address) {
		if (OrderStatus.WAITING != status) {
			throw new IllegalStateException("주문 대기 상태가 아닌 경우엔 주소를 변경할 수 없다.");
		}
		this.address = address;
	}

	public void issue() {
		++issuedCount;
	}
}
