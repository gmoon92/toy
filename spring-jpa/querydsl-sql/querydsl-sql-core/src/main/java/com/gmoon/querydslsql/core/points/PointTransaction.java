package com.gmoon.querydslsql.core.points;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_point_transaction")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointTransaction {

	@Id
	private String id;
	private String userId;
	private int amount;
	private String type;
	private LocalDateTime transactionAt;

	public PointTransaction(String userId, int amount, String type) {
		this.id = UUID.randomUUID().toString();
		this.userId = userId;
		this.amount = amount;
		this.type = type;
		this.transactionAt = LocalDateTime.now();
	}
}
