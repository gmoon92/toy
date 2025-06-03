package com.gmoon.querydslsql.core.points;

import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Collection;

import static com.gmoon.querydslsql.core.querydslsql.QTbPointTransaction.tbPointTransaction;

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
                    .set(tbPointTransaction.transactionAt, Timestamp.from(pointTransaction.getTransactionAt().toInstant(ZoneOffset.UTC)))
                    .addBatch();
        }
        insert.execute();
    }
}
