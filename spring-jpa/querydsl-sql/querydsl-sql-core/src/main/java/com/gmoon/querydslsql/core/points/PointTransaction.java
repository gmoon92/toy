package com.gmoon.querydslsql.core.points;

import com.gmoon.javacore.util.TsidUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
		this.id = TsidUtils.generate(4, 4, "-");
		this.userId = userId;
		this.amount = amount;
		this.type = type;
		this.transactionAt = LocalDateTime.now();
	}
}
