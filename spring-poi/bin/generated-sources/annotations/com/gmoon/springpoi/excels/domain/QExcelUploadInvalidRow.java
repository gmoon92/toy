package com.gmoon.springpoi.excels.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExcelUploadInvalidRow is a Querydsl query type for ExcelUploadInvalidRow
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExcelUploadInvalidRow extends EntityPathBase<ExcelUploadInvalidRow> {

    private static final long serialVersionUID = 1436757151L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExcelUploadInvalidRow excelUploadInvalidRow = new QExcelUploadInvalidRow("excelUploadInvalidRow");

    public final QExcelUploadTaskChunk chunk;

    public final SimplePath<com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages> errorMessages = createSimple("errorMessages", com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages.class);

    public final StringPath id = createString("id");

    public final SimplePath<com.gmoon.springpoi.excels.domain.vo.ExcelCellValues> originCellValues = createSimple("originCellValues", com.gmoon.springpoi.excels.domain.vo.ExcelCellValues.class);

    public final EnumPath<ExcelSheetType> sheetType = createEnum("sheetType", ExcelSheetType.class);

    public final EnumPath<ExcelInvalidRowType> type = createEnum("type", ExcelInvalidRowType.class);

    public QExcelUploadInvalidRow(String variable) {
        this(ExcelUploadInvalidRow.class, forVariable(variable), INITS);
    }

    public QExcelUploadInvalidRow(Path<? extends ExcelUploadInvalidRow> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExcelUploadInvalidRow(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExcelUploadInvalidRow(PathMetadata metadata, PathInits inits) {
        this(ExcelUploadInvalidRow.class, metadata, inits);
    }

    public QExcelUploadInvalidRow(Class<? extends ExcelUploadInvalidRow> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chunk = inits.isInitialized("chunk") ? new QExcelUploadTaskChunk(forProperty("chunk"), inits.get("chunk")) : null;
    }

}

