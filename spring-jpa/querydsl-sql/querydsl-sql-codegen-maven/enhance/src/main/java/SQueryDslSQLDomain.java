package com.meta;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.processing.Generated;
import java.sql.Timestamp;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * SQueryDslSQLDomain is a Querydsl query type for SQueryDslSQLDomain
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class SQueryDslSQLDomain extends com.querydsl.sql.RelationalPathBase<SQueryDslSQLDomain> {

    private static final long serialVersionUID = 1342421527;

    public static final SQueryDslSQLDomain pointtransaction = new SQueryDslSQLDomain("pointtransaction");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final StringPath id = createString("id");

    public final DateTimePath<Timestamp> transactionAt = createDateTime("transactionAt", Timestamp.class);

    public final StringPath type = createString("type");

    public final StringPath userId = createString("userId");

    public final com.querydsl.sql.PrimaryKey<SQueryDslSQLDomain> primary = createPrimaryKey(id);

    public SQueryDslSQLDomain(String variable) {
        super(SQueryDslSQLDomain.class, forVariable(variable), "null", "pointtransaction");
        addMetadata();
    }

    public SQueryDslSQLDomain(String variable, String schema, String table) {
        super(SQueryDslSQLDomain.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SQueryDslSQLDomain(String variable, String schema) {
        super(SQueryDslSQLDomain.class, forVariable(variable), schema, "pointtransaction");
        addMetadata();
    }

    public SQueryDslSQLDomain(Path<? extends SQueryDslSQLDomain> path) {
        super(path.getType(), path.getMetadata(), "null", "pointtransaction");
        addMetadata();
    }

    public SQueryDslSQLDomain(PathMetadata metadata) {
        super(SQueryDslSQLDomain.class, metadata, "null", "pointtransaction");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(transactionAt, ColumnMetadata.named("transactionAt").withIndex(2).ofType(Types.TIMESTAMP).withSize(26));
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(userId, ColumnMetadata.named("userId").withIndex(5).ofType(Types.VARCHAR).withSize(255));
    }

}

