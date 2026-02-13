package com.gmoon.springpoi.excels.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExcelUploadTask is a Querydsl query type for ExcelUploadTask
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExcelUploadTask extends EntityPathBase<ExcelUploadTask> {

    private static final long serialVersionUID = 1571047105L;

    public static final QExcelUploadTask excelUploadTask = new QExcelUploadTask("excelUploadTask");

    public final ListPath<ExcelUploadTaskChunk, QExcelUploadTaskChunk> chunks = this.<ExcelUploadTaskChunk, QExcelUploadTaskChunk>createList("chunks", ExcelUploadTaskChunk.class, QExcelUploadTaskChunk.class, PathInits.DIRECT2);

    public final BooleanPath completed = createBoolean("completed");

    public final DateTimePath<java.time.Instant> completedAt = createDateTime("completedAt", java.time.Instant.class);

    public final StringPath country = createString("country");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final StringPath filename = createString("filename");

    public final StringPath id = createString("id");

    public final NumberPath<Long> invalidRowCount = createNumber("invalidRowCount", Long.class);

    public final StringPath language = createString("language");

    public final StringPath originFilename = createString("originFilename");

    public final NumberPath<Long> processedRowCount = createNumber("processedRowCount", Long.class);

    public final DateTimePath<java.time.Instant> processingStartedAt = createDateTime("processingStartedAt", java.time.Instant.class);

    public final EnumPath<ExcelSheetType> sheetType = createEnum("sheetType", ExcelSheetType.class);

    public final StringPath signature = createString("signature");

    public final EnumPath<ExcelUploadTaskStatus> status = createEnum("status", ExcelUploadTaskStatus.class);

    public final StringPath timezone = createString("timezone");

    public final NumberPath<Long> totalRowCount = createNumber("totalRowCount", Long.class);

    public QExcelUploadTask(String variable) {
        super(ExcelUploadTask.class, forVariable(variable));
    }

    public QExcelUploadTask(Path<? extends ExcelUploadTask> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExcelUploadTask(PathMetadata metadata) {
        super(ExcelUploadTask.class, metadata);
    }

}

