package com.gmoon.querydslsql.core.points;

import static com.gmoon.querydslsql.querydslsql.QTbPointTransaction.*;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Collection;

import org.springframework.stereotype.Repository;

import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepository {

	private final SQLQueryFactory sqlQueryFactory;

	public void bulkInsert(Collection<PointTransaction> transactions) {
		SQLInsertClause insert = sqlQueryFactory.insert(tbPointTransaction);
		for (PointTransaction pointTransaction : transactions) {
			insert
				 .set(tbPointTransaction.id, pointTransaction.getId())
				 .set(tbPointTransaction.userId, pointTransaction.getUserId())
				 .set(tbPointTransaction.amount, pointTransaction.getAmount())
				 .set(tbPointTransaction.type, pointTransaction.getType())
				 .set(tbPointTransaction.transactionAt,
					  Timestamp.from(pointTransaction.getTransactionAt().toInstant(ZoneOffset.UTC)))
				 .addBatch();
		}
		insert.execute();
	}
}
