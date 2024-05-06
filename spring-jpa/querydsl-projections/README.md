# QueryDSL Projections

## Environment

- com.querydsl:querydsl-core:5.0.0

## Projection

[SQL SELECT 문](https://www.ibm.com/docs/en/informix-servers/14.10?topic=concepts-selection-projection)은 'SELECTION'과 '
PROJECTION'을 통해, 테이블의 행(rows)와 열(columns)을 제한하여 질의한다.

```sql
SELECT m.username, -- projection 구문
       m.password,
       m.*
FROM tb_member m
WHERE m.user LIKE 'gmoon%' -- selection 구문   
    LIMIT 0, 10;
```

- SELECTION: 행 제한, 다양한 조건을 사용하여 검색하고자 하는 행을 선택적으로 제한할 수 있다.
- PROJECTION: 열 제한, 테이블의 반환될 열을 선택할 수 있다.

## QueryDsl Projections

QueryDsl는 테이블의 특정 컬럼을 지정하여 반환할 수 있도록 Projection 관련 클래스를 지원한다.

- com.querydsl.core.types.Projections
    - bean
    - fields
    - constructor
- com.querydsl.core.annotations.@QueryProjection

타입 안정성을 보장한 Type Safe 한 프로그래밍 방식을 지원하는지, 불변 객체로 활용할 수 있는지에 대한 초점을 두면 좋을것 같다.

### com.querydsl.core.types.Projections

Projections 는 런타임 시점에 리플랙션을 활용하여 값을 주입하기 때문에 타입 안정성을 보장하지 않는다.

|             | 불변 객체 | 주 생성자 | setter | 비고                |
|-------------|-------|-------|--------|-------------------|
| bean        | X     | O     | O      | setter 주입 방식      |
| fields      | O     | O     | X      | field 주입 방식       |
| constructor | O     | X     | X      | constructor 주입 방식 |

- bean 방식은 접근제한자 수준은 public 해야하고, 주생성자와 setter 메서드가 필요하다.
    - 소스 객체와 타깃 객체(변환될 객체)의 setter 메서드 명은 동일해야 한다.
        - 다를 경우 null이 주입됨으로 이와 관련된 테스트 코드가 반드시 필요하다.
        - 명시적으로 Expressions.as 별칭을 주입할 타깃 필드명으로 지정한다.
        - `Expressions.as(movie.name, "movieName")`
- field 방식은 public 접근제한자의 주생성자가 필요하다.
    - 소스 객체의 필드 명과 타깃 객체(변환할 객체)의 필드 명은 동일해야 한다.
        - 다를 경우 null이 주입됨으로 이와 관련된 테스트 코드가 반드시 필요하다.
        - 명시적으로 Expressions.as 별칭을 주입할 타깃 필드명으로 지정한다.
        - `Expressions.as(movie.name, "movieName")`
- constructor 방식은 생성자의 인수 순서를 지켜야 한다.
    - 변환에 사용할 "부 생성자"의 접근제한자는 public 이어야 한다.
    - 컴파일 에러가 아닌, 런타입 에러가 발생하여 휴먼 이슈가 발생될 우려가 있다.
    - 롬북을 사용한다면, 빌드 패턴을 활용하기 위해 @AllArgsConstructor + @Builder 를 활용하여 객체를 구성할 수도 있다. 이때 멤버 필드의 순서가 변경되거나, 필드 사이에 새로운 상태가
      추가되면 런타임 에러 발생된다.

```java

@DataJpaTest(
	includeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE, classes = {
		JpaConfig.class,
		MovieRepositoryAdapter.class
	})
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryProjectionsTest {

	@Autowired
	private JPAQueryFactory queryFactory;

	@Test
	void bean() {
		MovieDTO result = queryFactory
			.select(
				Projections.bean(
					MovieDTO.class,
					movie.id,
					// movie.name, // 타깃 객체에 setName 필요.
					Expressions.as(movie.name, "movieName"),
					movie.genre
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@Test
	void fields() {
		MovieDTO result = queryFactory
			.select(
				Projections.fields(
					MovieDTO.class,
					movie.id,
					Expressions.as(movie.name, "movieName"),
					movie.genre
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@Test
	void constructor() {
		MovieDTO result = queryFactory
			.select(
				Projections.constructor(
					MovieDTO.class,
					movie.id,
					movie.name,
					movie.genre
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(result.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(result.getMovieName()).isEqualTo("범죄도시4");
		assertThat(result.getGenre()).isEqualTo(MovieGenre.ACTION);
	}

	@NoArgsConstructor(access = AccessLevel.PUBLIC)
	@Getter
	@Setter(AccessLevel.PUBLIC)
	public static class MovieDTO {

		private String id;
		private String movieName;
		private MovieGenre genre;

		public MovieDTO(String id, String movieName, MovieGenre genre) {
			this.id = id;
			this.movieName = movieName;
			this.genre = genre;
		}
	}
}
```

### com.querydsl.core.annotations.@QueryProjection

`Projections.constructor` 의 단점은 명확하다.

- 반드시 생성자의 인수 타입과 순서를 보장해야 한다.
- 순서를 보장하지 않을 시 런타임시 예외가 발생된다.

@QueryProjection 방식은 `Projections.constructor` 를 보안한 방식으로, 제네릭의 불변성 특성을 활용하여 컴파일 에러를 발생시키는 방식이다.

1. 생성자에 어노테이션을 선언을 하게되면 지정된 생성자와 함께 ConstructorExpression 를 확장한 Querydsl Projection 클래스가 생성된다.
2. QObject 생성자 인수 타입은 Expression<Type> 제네릭 클래스로 감싸 생성된다.
    - 제네릭 불변성을 활용하여 컴파일 에러를 발생시키는 방식.
3. `Projections.constructor` 에서 방식과 동일하게 ConstructorExpression 클래스를 통해 타깃 클래스의 데이터를 주입 받도록 설계됐다.

```java

@Getter
public class MovieResponse {

	private String id;
	private String movieName;
	private MovieGenre genre;
	private FilmRatings filmRatings;
	private String directorName;
	private Long releaseDateTime;

	// 1. @QueryProjection 선언
	@QueryProjection
	public MovieResponse(String id, String movieName, MovieGenre genre,
		FilmRatings filmRatings, String movieDirectorName, MovieReleaseTime movieReleaseTime) {
		this.id = id;
		this.movieName = movieName;
		this.genre = genre;
		this.filmRatings = filmRatings;
		this.directorName = movieDirectorName;
		this.releaseDateTime = movieReleaseTime.toSeconds();
	}
}
```

`select` 구문에 생성된 Querydsl projection 객체를 생성자 인수에 맞게 지정해주면 된다.

```java
public class QueryProjectionsTest {

	@Test
	void queryProjectionsAnnotation() {
		MovieResponse response = queryFactory
			.select(
				new QMovieResponse(
					movie.id,
					movie.name,
					movie.genre,
					movie.filmRatings,
					movie.director.director.name,
					movie.releaseTime
				)
			)
			.from(movie)
			.where(movie.id.eq(Fixtures.MOVIE_ID_001))
			.fetchFirst();

		assertThat(response.getId()).isEqualTo(Fixtures.MOVIE_ID_001);
		assertThat(response.getMovieName()).isEqualTo("범죄도시4");
		assertThat(response.getGenre()).isEqualTo(MovieGenre.ACTION);
	}
}
```

## 마무리

- Projections 보단 @QueryProjection 을 활용해라.
- Projections + DTO

만약 Projections 를 활용한다면 타깃 객체는 DTO 객체만 활용하는 편이 좋다.

타깃 객체가 엔티티 객체인 경우 개발자의 휴면 이슈를 발생시킬 위험이 있다.
상위 계층에서 하위 계층의 메서드를 호출하게 될 경우 명시된 인터페이스의 리턴 타입과 메서드 명을 기준으로 활용할 기능을 선택하게 된다. 내부 구현을 보지 않는 이상, 호출한 계층에선 엔티티 객체의 일부 상태 값들이
null 로 주입될 수 있다는 점을 판단할 수 없다. 따라서 특정 상태 값이 없는 엔티티 객체의 메서드는 런타임 시점에 NPE 예외로 전파될 위험이 있다.

## Reference

- [Hibernate 5.4 - User Guide](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html)
- [Hibernate 5.4 - Java docs](https://docs.jboss.org/hibernate/orm/5.4/javadocs/)
