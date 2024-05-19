package com.gmoon.springjpalock.orders.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import com.gmoon.springjpalock.orders.domain.vo.OrderStatus;

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
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(length = 50)
	private String no;

	@Column(name = "address", length = 50)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 50)
	private OrderStatus status;

	@Version
	@Setter
	private Long version;

	@ColumnDefault("0")
	@OptimisticLock(excluded = true)
	@Column(name = "issued_count")
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
