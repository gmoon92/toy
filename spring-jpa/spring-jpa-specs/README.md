# Spring Data JPA Specification

- 스펙(Specification)이란 기술 표준을 뜻한다.
- 스펙은 디자인, 제품 또는 서비스에 대한 문서화된 요구 사항 집합을 의미한다.
- DDD 는 비즈니스 로직을 중점으로 개발을 진행하는데, 스펙이란 도메인의 해결 영역(요구 사항)의 집합을 정의한다.
- 스펙은 애그리거트가 특정 조건을 충족하는지 여부를 검사한다.
- 리포지터리는 스펙을 전달받아 애그리거트를 걸러내는 용도로 사용한다.
- 스펙의 장점은 조합에 있다.

## Concepts

스펙 패턴은 부울 논리를 사용하여 비즈니스 규칙을 함께 연결하여 비즈니스 규칙을 다시 결합할 수 있는 디자인 패턴이다.

- Expressions: Domain Knowledge Expression
- Logic Operators: AND(&&), OR(||)
- Compound expression: Connection of expressions & logic operators

## Specification

스펙
패턴은 [컴포짓 패턴(Composite Pattern)](https://github.com/gmoon92/gof-design-pattern/blob/main/02.Structural%20Patterns/08.Composite.md)
을 기반으로 도메인 지식과 논리 연산자를 결합한다.

- Java with Composite Pattern
- ~~[Criteria](https://www.inflearn.com/questions/16685)~~
- QueryDSL
    - QuerydslPredicateExecutor
    - BooleanBuilder
    - @QueryDelegate

### Java Spec with Composite Pattern

컴포짓 패턴 기반의 스펙 패턴은 다음과 같다.

- Component
    - Specification
- Leaf
    - NotSpecification
- Composite
    - AndSpecification
    - OrSpecification

```java
// Component
public interface Specification<T> {

	// 인수는 검색 대상이 되는 애그리거트 루트
	boolean isSatisfiedBy(T aggregateRoot);
}

// Leaf
public class NotSpecification<T>
  implements Specification<T> {

	private final Specification<T> value;

	public NotSpecification(Specification<T> spec) {
		value = spec;
	}

	@Override
	public boolean isSatisfiedBy(T aggregateRoot) {
		return !value.isSatisfiedBy(aggregateRoot);
	}
}

// Composite
public class OrSpecification<T>
  implements Specification<T> {

	private final List<Specification<T>> values;

	public OrSpecification(Specification<T>... specs) {
		values = Arrays.asList(specs);
	}

	@Override
	public boolean isSatisfiedBy(T aggregateRoot) {
		for (Specification<T> spec : values) {
			if (spec.isSatisfiedBy(aggregateRoot)) {
				return true;
			}
		}

		return false;
	}
}

public class AndSpecification<T>
  implements Specification<T> {

	private final List<Specification<T>> values;

	public AndSpecification(Specification<T>... specs) {
		values = Arrays.asList(specs);
	}

	@Override
	public boolean isSatisfiedBy(T aggregateRoot) {
		for (Specification<T> spec : values) {
			if (!spec.isSatisfiedBy(aggregateRoot)) {
				return false;
			}
		}

		return true;
	}
}

// 스펙 생성 팩토리 클래스
public final class Specs {

	private Specs() {
		throw new UnsupportedOperationException();
	}

	public static <T> Specification<T> and(Specification<T>... specs) {
		return new AndSpecification<>(specs);
	}

	public static <T> Specification<T> or(Specification<T>... specs) {
		return new OrSpecification<>(specs);
	}

	public static <T> Specification<T> not(Specification<T> spec) {
		return new NotSpecification<>(spec);
	}
}
```

### 스펙과 복잡한 검색 조건

스펙의 조합으로 복잡한 검색 조건을 해결한다.

```java
public class OrderService {

	private final OrderRepository orderRepository;

	public List<Order> findAll(String orderer, LocalDate from, LocalDate to) {
		Specification<Order> ordererSpec = new OrdererSpec(orderer);
		Specification<Order> orderDateSpec = new OrderDateSpec(from, to);

		// 스펙 조합
		Specification<T> spec = Specs.and(ordererSpec, orderDateSpec);

		return orderRepository.findAll().stream()
		  .filter(spec::isSatisfiedBy)
		  .collect(toList());
	}
}

public class OrdererSpec implements Specification<Order> {

	private String orderer;

	public OrdererSpec(String orderer) {
		this.orderer = orderer;
	}

	// 검색 조건을 충족하면 true 리턴
	@Override
	public boolean isSatisfiedBy(Order aggregateRoot) {
		return aggregateRoot.getName()
		  .equals(orderer);
	}
}
```

- 장점
    - 도메인에 대한 비즈니스 로직 응집도 증가
    - 비즈니스 로직 재사용성
    - 다양한 조건 결합 가능
- 단점
    - 실행 속도 문제
    - 메모리 문제, OOM 발생 가능성이 높다.
    - 성능 이슈 발생

### JPA Spec with QueryDSL

Java 기반의 스펙 방식은 조회된 데이터 수에 따라 힙 메모리 영향을 미친다.

Spring Data Repository 에는 메서드 이름으로 쿼리를 생성하는 메커니즘이 존재한다.

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Customer findByEmailAddress(String emailAddress);

	List<Customer> findByLastname(String lastname, Sort sort);

	Page<Customer> findByFirstname(String firstname, Pageable pageable);
}
```

쿼리에 대한 자동 생성한다는 장점도 있지만, 다양한 술어를 지원하기 위해 여러 메서드를 구성해야 한다.

- 다양한 검색 조건을 충족하기 위한 쿼리 메서드 수가 증가한다.
- 고정된 쿼리 형식으로, 다양한 atomic 한 술어를 지원하지 않는다.

### [QuerydslPredicateExecutor](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#core.extensions.querydsl)

Spring Data 에선 다양한 술어를 지원하면서 QueryDSL 를 쉽게 확장할 수 있도록 QuerydslPredicateExecutor 를 지원하고 있다.

- org.springframework.data.querydsl.QuerydslPredicateExecutor

```java
package org.springframework.data.querydsl;

public interface QuerydslPredicateExecutor<T> {

	Optional<T> findOne(Predicate predicate);

	Iterable<T> findAll(Predicate predicate);

	Iterable<T> findAll(Predicate predicate, Sort sort);

	Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

	Iterable<T> findAll(OrderSpecifier<?>... orders);

	Page<T> findAll(Predicate predicate, Pageable pageable);

	long count(Predicate predicate);

	boolean exists(Predicate predicate);

	<S extends T, R> R findBy(Predicate predicate, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);
}
```

레파지토리 인터페이스에 QuerydslPredicateExecutor 를 확장시켜주면 된다.

```java
public interface JpaBookStoreRepository
  extends JpaRepository<BookStore, String>,
        QuerydslPredicateExecutor<BookStore> {

}
```

### BooleanBuilder

술어(Predicate) 구성은 Java Spec 처럼 컴포짓 패턴을 사용하는것 보단, QueryDSL 의 `com.querydsl.core.BooleanBuilder` 를 활용하자.

- 여러 Predicate 조합 가능
- 논리 연산자(Logic Operators) 조합 가능
- NPE 방어

### @QueryDelegate

@QueryDelegate 는 커스텀 QDomain 경로(Path)를 생성할 수 있다.

이를 활용하여 도메인에 대한 특화된 용어(DSL) 을 정의할 수 있다.

예를 들어 메뉴에 대해 할인 메뉴와 전시된 메뉴가 존재한다고 가정해보자.

```java
@NoArgsConstructor(access = PRIVATE)
public final class MenuExpressions {

  @QueryDelegate(Menu.class)
  public static Predicate isDisplayed(QMenu menu) {
    return menu.status.in(MenuStatus.DISPLAY);
  }

  @QueryDelegate(Menu.class)
  public static Predicate usableDiscountPeriod(QMenu menu) {
    LocalDateTime now = LocalDateTime.now();
    return new BooleanBuilder()
            .and(menu.option.discountStartDate.loe(now))
            .and(menu.option.discountEndDate.goe(now));
  }
}
```

전시된 메뉴 중 할인 중인 메뉴를 조회하고 싶다면, `QuerydslPredicateExecutor`를 확장한 레파지토리에 술어(Predicate)를 생성 전달해주면 된다.

```java
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class MenuService {

  private JpaMenuRepository repository;

  public List<Menu> discountMenus() {
    QMenu menu = QMenu.menu;
    Predicate isDiscountMenu = new BooleanBuilder()
            .and(menu.isDisplayed())
            .and(menu.usableDiscountPeriod());

    return repository.findAll(isDiscountMenu);
  }
}
```

## 마무리

스펙 패턴으로 조회시 다양한 술어를 대응할 수 있다.

- 실무에선 보다 복잡한 조회 쿼리를 요구하는 경우가 많다.
- 하지만 스펙은 복잡한 쿼리일 수록 난잡해질 경향이 높다.

이럴 경우 스펙을 활용하기 보단 직관적인 QueryDsl 의 BooleanBuilder 와 @QueryDelegate 를 활용한 방법을 사용한다.

## Reference

- [Wiki - Specification pattern](https://en.wikipedia.org/wiki/Specification_pattern)
- [Spring blog - Advanced Spring Data JPA - Specifications and Querydsl](https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/)
- [Domain-Driven Design: Specification Pattern](https://medium.com/@pawel_klimek/domain-driven-design-specification-pattern-82867540305c)
- [Specification Pattern vs Always-Valid Domain Model](https://enterprisecraftsmanship.com/posts/specification-pattern-always-valid-domain-model/)
- [stackoverflow - Replacing deprecated QuerydslJpaRepository with QuerydslJpaPredicateExecutor fails](https://stackoverflow.com/questions/53083047/replacing-deprecated-querydsljparepository-with-querydsljpapredicateexecutor-fai)
- [querydsl reference](http://querydsl.com/static/querydsl/4.0.1/reference/ko-KR/html_single/)
  - [querydsl - codegen](http://querydsl.com/static/querydsl/4.0.1/reference/ko-KR/html_single/#codegen)
