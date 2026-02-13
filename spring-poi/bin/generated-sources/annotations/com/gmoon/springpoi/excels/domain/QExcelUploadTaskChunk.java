package com.gmoon.springpoi.excels.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExcelUploadTaskChunk is a Querydsl query type for ExcelUploadTaskChunk
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExcelUploadTaskChunk extends EntityPathBase<ExcelUploadTaskChunk> {

    private static final long serialVersionUID = 1229978668L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExcelUploadTaskChunk excelUploadTaskChunk = new QExcelUploadTaskChunk("excelUploadTaskChunk");

    public final NumberPath<Integer> attemptCount = createNumber("attemptCount", Integer.class);

    public final BooleanPath completed = createBoolean("completed");

    public final DateTimePath<java.time.Instant> completedAt = createDateTime("completedAt", java.time.Instant.class);

    public final NumberPath<Long> endIdx = createNumber("endIdx", Long.class);

    public final StringPath errorMessage = createString("errorMessage");

    public final StringPath id = createString("id");

    public final NumberPath<Long> invalidRowCount = createNumber("invalidRowCount", Long.class);

    public final NumberPath<Long> processedRowCount = createNumber("processedRowCount", Long.class);

    public final DateTimePath<java.time.Instant> processingStartedAt = createDateTime("processingStartedAt", java.time.Instant.class);

    public final EnumPath<ExcelSheetType> sheetType = createEnum("sheetType", ExcelSheetType.class);

    public final NumberPath<Long> startIdx = createNumber("startIdx", Long.class);

    public final EnumPath<ExcelUploadChunkStatus> status = createEnum("status", ExcelUploadChunkStatus.class);

    public final QExcelUploadTask task;

    public final NumberPath<Long> totalRowCount = createNumber("totalRowCount", Long.class);

    public QExcelUploadTaskChunk(String variable) {
        this(ExcelUploadTaskChunk.class, forVariable(variable), INITS);
    }

    public QExcelUploadTaskChunk(Path<? extends ExcelUploadTaskChunk> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExcelUploadTaskChunk(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExcelUploadTaskChunk(PathMetadata metadata, PathInits inits) {
        this(ExcelUploadTaskChunk.class, metadata, inits);
    }

    public QExcelUploadTaskChunk(Class<? extends ExcelUploadTaskChunk> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.task = inits.isInitialized("task") ? new QExcelUploadTask(forProperty("task")) : null;
    }

}

