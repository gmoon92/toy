# Hibernate Issue

### 현상: 자식 테이블 저장 시 Enum 타입이 `null`로 저장됨

```text
[Hibernate] insert into excel_upload_task_chunk (...) values (?, ?, ..., ?)
...
org.hibernate.orm.jdbc.bind              : binding parameter (9:ENUM) <- [null]
...
SQL Error: 1048, SQLState: 23000
Column 'sheet_type' cannot be null
```

### 엔티티 구조

```java

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class ExcelUploadTask {
	@Id
	@Column(length = ColumnLength.ID)
	private String id;

	@OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private List<ExcelUploadTaskChunk> chunks = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	public ExcelUploadTask(ExcelSheetType sheetType, long totalRowCount, long chunkSize) {
		this.id = UUID.randomUUID().toString();
		this.sheetType = sheetType;
		this.chunks = createChunks(totalRowCount, chunkSize);
	}
}
```

```java

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExcelUploadTaskChunk {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ExcelUploadTask task;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	public ExcelUploadTaskChunk(ExcelUploadTask task) {
		this.task = task;
		this.sheetType = task.getSheetType();
	}
}
```

### 원인 분석

**애플리케이션 수준에서 ID를 직접 생성**

```java
public ExcelUploadTask(ExcelSheetType sheetType, long totalRowCount, long chunkSize) {
	this.id = UUID.randomUUID().toString();
}
```

Hibernate는 엔티티를 신규 객체로 인식하지 않고, `merge`(UPSERT) 방식으로 처리합니다.

**CascadeType.PERSIST와 함께 Chunk 생성**

- Task 생성 시 Chunk도 같이 생성됨
- merge(UPSERT) 방식으로 동작하기 때문에, Hibernate가 기존 엔티티를 조회 후 저장하는 과정에서 **컬렉션 필드는 프록시 객체로 대체**됩니다.
- 이 과정에서 이미 할당된 값(`sheetType`)이 초기화 시점에 덮어써져 `null`로 저장되는 현상이 발생합니다.

#### **ID 타입이 원시 타입이 아닌 경우 Hibernate 동작**

```java
// 값이 존재하면 isNew=false -> merge 호출
if (!idType.isPrimitive()) {
   return id == null; 
}
```


### 해결 방안

- **애플리케이션에서 ID를 할당하지 않고 Hibernate ID 생성 전략 사용**

```java

@Entity
public class ExcelUploadTask {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME) // Hibernate가 자동으로 UUID 생성
	@Column(length = ColumnLength.ID)
	private String id;
}
```

- Hibernate가 객체를 신규로 인식하고 `persist`로 처리되므로, 컬렉션 값이 정상적으로 저장됩니다.
- 애플리케이션에서 미리 ID를 채번해야될 경우, 
