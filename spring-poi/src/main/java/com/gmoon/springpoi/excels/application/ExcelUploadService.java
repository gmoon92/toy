package com.gmoon.springpoi.excels.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.gmoon.springpoi.common.excel.config.ExcelUploadProperty;
import com.gmoon.springpoi.common.excel.exception.ExcelEmptyDataRowException;
import com.gmoon.springpoi.common.excel.exception.NotFoundExcelFileException;
import com.gmoon.springpoi.common.excel.helper.ExcelHelper;
import com.gmoon.springpoi.common.excel.processor.ExcelRowProcessor;
import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.common.exception.InvalidFileException;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
import com.gmoon.springpoi.excels.domain.ExcelUploadTask;
import com.gmoon.springpoi.excels.domain.ExcelUploadTaskChunk;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelUploadService {
	private final ApplicationContext ctx;

	private final ExcelUploadTaskService taskService;

	private final ExcelHelper excelHelper;
	private final ExcelUploadProperty property;

	public ExcelUploadTask getTask(String id) {
		return taskService.getTask(id);
	}

	public ExcelUploadTask upload(File file, ExcelSheetType sheetType, Locale locale, String timezone) {
		Path uploadedExcelFile = getExcelFilePath(file);

		long dataRowCount = getDataRowCount(sheetType, uploadedExcelFile);
		ExcelUploadTask saved = taskService.save(
			 sheetType,
			 dataRowCount,
			 file.getName(),
			 locale,
			 timezone
		);

		uploadExcelFile(saved, file);
		return saved;
	}

	private Path getExcelFilePath(File file) {
		if (file == null || !file.exists()) {
			throw new NotFoundExcelFileException(file);
		}

		if (!file.canRead()) {
			throw new InvalidFileException("File is not readable!");
		}

		String filename = file.getName().toLowerCase();
		if (!filename.endsWith(".xlsx")) {
			throw new InvalidFileException("Only .xlsx files are allowed: " + filename);
		}

		return Paths.get(file.getAbsolutePath());
	}

	private long getDataRowCount(ExcelSheetType sheetType, Path path) {
		long dataRows = excelHelper.getDataRows(path, sheetType, property.getMaxRows());
		if (dataRows == 0) {
			throw new ExcelEmptyDataRowException();
		}
		return dataRows;
	}

	private void uploadExcelFile(ExcelUploadTask task, File originalFile) {
		String filename = task.getFilename();
		Path storagePath = Paths.get(property.getStoragePath());
		Path target = task.getExcelUploadPath(storagePath);
		try {
			Path parent = target.getParent();
			Files.createDirectories(parent);

			Path source = originalFile.toPath();
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload file: " + filename, e);
		}
	}

	public void processing(String chunkId) {
		ExcelUploadTaskChunk chunk = taskService.getChunk(chunkId);
		ExcelSheetType sheetType = chunk.getSheetType();
		ExcelUploadTask task = chunk.getTask();
		ExcelRowProcessor<?> processor = ctx.getBean(sheetType.getExcelRowProcessorClass());

		try {
			Path excelFilePath = getExcelFilePath(task);

			excelHelper.readSAX(
				 excelFilePath,
				 sheetType.getExcelModelClass(),
				 (originRows, rows, invalidRows) -> {
					 handleRows(chunk, originRows, rows, processor);

					 taskService.saveInvalidRows(chunk.getId(), invalidRows);
				 },
				 chunk.getStartIdx(),
				 chunk.getEndIdx()
			);
		} catch (Exception e) {
			taskService.failChunk(chunkId, e.getMessage());
		}
	}

	/**
	 * 얕은(Shallow) 제네릭 헬퍼 메서드
	 *
	 * <p>제네릭 기반 설계는 <b>Checked Exception</b>과 유사하게 이해할 수 있습니다.
	 * <b>Checked Exception</b>은 중간에서 처리하지 않으면 호출부로 전파되지만,
	 * <b>UnChecked Exception</b>으로 래핑하면 전파를 끊을 수 있습니다.
	 * 제네릭도 마찬가지로, 적절한 호출부에서 타입 전파를 끊지 않으면 계속 호출부로 이어집니다.</p>
	 *
	 * <p>이 경우 Object로 반환하여 강제 타입 캐스팅을 하거나,
	 * 타입 파라미터 추적이 불가할 때,
	 * 얕은 제네릭(Shallow Generic) 혹은 헬퍼 메서드를 통해 "전파를 끊는 것"과 유사한 설계가 필요합니다.</p>
	 *
	 * <p><b>이는 런타임 타입 소거(Type Erasure)를 활용하여,
	 * 의도적으로 raw 타입을 사용해 타입 전파를 끊는 패턴입니다.</b></p>
	 *
	 * <p>얕은 제네릭 또는 헬퍼 메서드를 통해 타입 전파를 끊는 것은,
	 * 정확한 타입 체크가 불가피하게 어려운 바운더리(외부 API, Bean, DI 컨테이너 등)에서
	 * 최소한의 범위로 제한해서 사용해야 합니다.</p>
	 *
	 * <p>컴파일러가 보장하던 제네릭 타입 안전성을 의도적으로 우회하는 설계이므로,
	 * 일반적으로 Object 반환이나 unchecked cast 대신,
	 * 인스턴스 생성 시 상한 제한 와일드카드(? extends ...)를 사용해
	 * 런타임 타입 캐스팅 오류를 방지하는 것이 핵심입니다.</p>
	 *
	 * <p>얕은 제네릭(Shallow Generic) 패턴과 unchecked cast는 반드시 코드상에서 용도와 위험을
	 * 주석 등으로 명확하게 남기고, 검증된 경로에서만 제한적으로 사용하는 것이 좋습니다.</p>
	 *
	 * <ul>
	 *     <li>메서드 제네릭 선언은 호출부에서 타입 추론이 불가능할 수 있으므로, 클래스 레벨 제네릭 선언을 권장</li>
	 *     <li>람다/함수형 인터페이스에서 타입 추론이 안 되는 경우가 많아 클래스 제네릭이 더 안전</li>
	 *     <li>실무에서는 외부 API, 컨테이너, 프레임워크 등 제한으로 인해 얕은 제네릭 래퍼에서만 타입 전파를 끊는 경우가 존재</li>
	 * </ul>
	 *
	 * <p>메서드만의 제네릭 타입은 람다/함수형 인터페이스 등에서 추론이 안 되어
	 * "Target method is generic"과 같은 에러가 발생할 수 있으며,
	 * 이때는 클래스(또는 인터페이스) 전체에 타입 파라미터를 선언하는 것이 더 안전합니다.</p>
	 *
	 * 예를 들어 다음과 같이 {@code RowCallbackHandler#handle} 메서드가 제네릭 타입으로 선언된 경우
	 * readSAX 메서드 호출하면 "Target method is generic" 같은 컴파일 에러가 발생됩니다.
	 *
	 * <pre>{@code
	 * @FunctionalInterface
	 * public interface RowCallbackHandler {
	 *     <T extends BaseExcelModel> void handle(
	 *         Set<ExcelRow<T>> rows
	 *     );
	 * }
	 * }</pre>
	 *
	 * <pre>{@code
	 * public <T extends BaseExcelModel> ExcelSheet<T> readSAX(
	 *     RowCallbackHandler rowCallbackHandler
	 * ) {
	 * }
	 * }</pre>
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void handleRows(
		 ExcelUploadTaskChunk chunk,
		 Map<Long, ExcelCellValues> originRows,
		 Set<? extends ExcelRow<? extends BaseExcelModel>> rows,
		 ExcelRowProcessor<? extends BaseExcelModel> processor
	) {
		for (ExcelRow<? extends BaseExcelModel> row : rows) {
			processor.doProcess(chunk.getId(), originRows, (ExcelRow)row);
		}
	}

	private Path getExcelFilePath(ExcelUploadTask task) {
		String filePath = property.getStoragePath();
		String filename = task.getFilename();
		return Paths.get(filePath, filename);
	}

	public void startProcessing(String taskId, String chunkId) {
		ExcelUploadTask task = taskService.startProcessing(taskId, chunkId);
		log.info("Starting processing task: {}, chunk: {}", task.getId(), chunkId);
	}

	public ExcelUploadTask updateTaskSummary(String chunkId) {
		ExcelUploadTaskChunk chunk = taskService.completeChunk(chunkId);

		ExcelUploadTask task = taskService.updateTaskSummary(chunk.getTaskId());
		removeExcelFile(task);
		return task;
	}

	private void removeExcelFile(ExcelUploadTask task) {
		if (!task.isCompleted()) {
			return;
		}

		try {
			Path storagePath = Paths.get(property.getStoragePath());
			Path excelUploadPath = task.getExcelUploadPath(storagePath);
			Files.delete(excelUploadPath);
		} catch (IOException e) {
			log.error("Failed to delete excel file: {}", task.getFilename(), e);
		}
	}
}
