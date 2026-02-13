package com.gmoon.springquartzcluster.server;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QServer is a Querydsl query type for Server
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QServer extends EntityPathBase<Server> {

    private static final long serialVersionUID = 1227871647L;

    public static final QServer server = new QServer("server");

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> port1 = createNumber("port1", Integer.class);

    public final NumberPath<Integer> port2 = createNumber("port2", Integer.class);

    public final NumberPath<Integer> port3 = createNumber("port3", Integer.class);

    public final StringPath privateHost = createString("privateHost");

    public final StringPath publicHost = createString("publicHost");

    public final EnumPath<ServerType> type = createEnum("type", ServerType.class);

    public QServer(String variable) {
        super(Server.class, forVariable(variable));
    }

    public QServer(Path<? extends Server> path) {
        super(path.getType(), path.getMetadata());
    }

    public QServer(PathMetadata metadata) {
        super(Server.class, metadata);
    }

    public BooleanExpression isEnabled() {
        return Server.isEnabled(this);
    }

}

