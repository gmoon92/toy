# Hibernate inheritance

상속은 Java 의 핵심 개념중 하나다. 이와 마찬가지로 도메인 모델에서도 상속이라는 개념을 사용할 수 있지만, 기본적으로 관계형 데이터베이스에선 상속 구조를 지원하지 않는다.

이를 해결하기 위해 JPA의 구현체들은 도메인 상속 모델링 구조를 지원하기 위해 다양한 전략을 제공하고 있다.

## 상속 구조 모델링 4 가지 전략

하이버네이트에선 도메인 모델의 상속 구조를 다른 테이블 구조에 매핑하는 4가지 전략을 제공한다.

- [Mapped Superclass](#mapped-superclass)
- Single Table
- Joined Table
- Table per Class

### Mapped Superclass

Mapped Superclass 전략은 상속 구조를 데이터베이스 테이블에 매핑하는 가장 간단한 접근 방식이다.

엔터티 간에 상태 및 매핑 정보를 공유하려는 경우, `@MappedSuperclass` 전략이 적합하고 구현하기 쉽다.

- 고차원적인 도메인 로직을 일반화한 추상화 클래스를 제공한다.
- 상위 클래스 제공하지만 상위 클래스는 엔티티는 아니다.(@Entity 어노테이션이 정의되어 있지 않다.)
  - 구현체들은 @MappedSuperclass 추상화 클래스의 필드를 포함한 하나의 테이블로 생성된다.
  - 상위 클래스와 다양한 구현체간의 테이블 관계를 표현할 수 없다.
  - 다향성 쿼리를 조회할 수 없다.

```java
// @MappedSuperclass 정의하지 않으면 
// Hibernate는 슈퍼클래스의 매핑 정보 무시
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Product {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private ProductType type;

  // constructor, getter, setter... 
}

@Entity
public class CompanyProduct extends Product { }

@Entity
public class UserProduct extends Product {
  
  private String gender;
}
```

`@MappedSuperclass` 전략 방식은 상위 클래스를 별도로 테이블로 관리를 하지 않음으로, 아래와 같이 Product 도메인과 식별 관계인 Price 연관 관계를 설정할 수 없다는 단점이 존재한다.  

```java
@MappedSuperclass
public abstract class Product {
  
  // 불가
  @OneToOne(mappedBy = "product", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
  private Price price;
}
```

### JPA @Inheritance 전략

- 상위 클래스
  - @Inheritance: 상속 구현 전략 명시
    - Single Table (default)
    - Joined
    - Table per class
  - @DiscriminatorColumn: 구분 컬럼 명
  - @DiscriminatorFormula
- 구현 클래스
  - @DiscriminatorValue: 구분 컬럼 값
    - @DiscriminatorValue("null")
      - 판별자 값이 null인 경우, "null" 속성 값이 정의된 엔티티 클래스와 맵핑. 상속 계층의 루트 클래스에 적용될 수 있다.
    - @DiscriminatorValue("not null")
      - 모든 엔티티 클래스에 정의된 판별자 값과 비교시 매칭되지 않는 값일 경우, "not null" 속성 값이 정의된 엔티티 클래스와 맵핑 

### Single Table

Single Table 전략은 상위 클래스 포함, 모든 구현체 클래스에 정의된 컬럼을 단 하나의 테이블을 생성해서 관리한다.

- Single Table 전략은 사용하기 가장 쉽고 효율적인 데이터 접근 제공 
- 각 엔터티의 모든 속성은 상위 클래스에 명시된 하나의 테이블에 생성되고 관리된다.
- dtype(Discriminator Column Type) 컬럼으로 각 엔티티 데이터 구분
  - 조회 쿼리에 Join 절이 필요 없다. 조회 쿼리가 단순하다.
- 하나의 테이블에서 인덱스, 제약 조건을 관리하기 때문에 각 엔티티별 확장 포인트가 많다면 관리하기 어려워질 수 있다. 
  - 각 서브 클래스의 모든 컬럼을 하나의 테이블에서 관리하기 때문에 컬럼 수에 따른 성능 이슈가 발생할 수 있다.
  - 각 서브 클래스의 컬럼마다 제약 조건을 재정의할 수 없다.

```java
@Entity
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Product implements Serializable {
  // fields, constructor, getter, setter...
}

@Entity
@DiscriminatorValue("C")
public class CompanyProduct extends Product { }

@Entity
@DiscriminatorValue("U")
public class UserProduct extends Product {

  @Column(nullable = false)
  private String gender;
}
```

- @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
- @DiscriminatorColumn
- @DiscriminatorValue

다음과 같이 Single Table 전략의 특징으로 UserProduct 도메인에 추가된 gender 컬럼을 포함하여 Product(상위 클래스) 도메인 테이블 생성 DDL 문을 살펴볼 수 있다.

```sql
CREATE TABLE product
(
  id     BIGINT      NOT NULL,
  dtype  VARCHAR(31) NOT NULL, -- 엔티티 데이터 구분 컬럼
  name   VARCHAR(255),
  type   VARCHAR(255),
  gender VARCHAR(255) NOT NULL, -- 사용자 제품 도메인의 제약 조건
  PRIMARY KEY (id)
)
```

### Joined Table

Joined Table 전략은 엔티티 클래스를 각각의 별도의 테이블로 생성해서 관리한다.

- 상위 클래스와 구현 클래스는 식별 관계로 생성된다.
  - 상위 클래스의 기본 키를 각 구현 클래스 공유
- 구현 클래스의 컬럼 제약조건 설정이 자유롭고, 효율적인 저장공간을 활용할 수 있다.
- 구현 클래스를 쿼리하려면 상위 테이블과 조인해야된다.
  - Single Table 전략과 비교시, 상대적으로 복잡한 조회 쿼리를 요구하기 때문에, 약간의 오버헤드가 발생할 수 있다.

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 구현 전략
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Product implements Serializable {
  // fields, constructor, getter, setter...
}
```

```sql
CREATE TABLE product
(
    id    BIGINT      NOT NULL,
    name  VARCHAR(255),
    type  VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE company_product
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_product
(
    id     BIGINT       NOT NULL,
    gender VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- Product 의 기본키를 
-- 자식 엔티티의 기본키이면서 외래키로 사용하는 식별 관계
ALTER TABLE company_product ADD CONSTRAINT FK3vtq8bb7etcmk0gtn9n9ek4gm FOREIGN KEY (id) REFERENCES product (id);
ALTER TABLE user_product ADD CONSTRAINT FKhigxfmc670obcywfpc4eiu1ld FOREIGN KEY (id) REFERENCES product (id);
```

`@PrimaryKeyJoinColumn`를 사용하면 상위 엔티티(Product)의 기본 키에 대한 외래 키 제약 조건을 재정의할 수 있다.

```java
@Entity
@PrimaryKeyJoinColumn(name = "company_product_id")
public class CompanyProduct extends Product {
  // fields, constructor, getter, setter...
}
```

```sql
CREATE TABLE company_product
(
    company_product_id BIGINT NOT NULL,
    PRIMARY KEY (company_product_id)
);
ALTER TABLE company_product ADD CONSTRAINT FK2k5qwrpewhnahacpbxyyrlcvp FOREIGN KEY (company_product_id) REFERENCES product (id)
```

### Table per Class

Table per Class 전략은 하위 엔티티 클래스에 해당하는 테이블을 각각 정의하고, 상위 클래스에 정의된 필드를 각 하위 구현 엔티티 클래스별로 테이블에 추가하는 방식이다.

상위 클래스의 모든 속성이 해당 테이블에 있으므로 조인이 필요하지 않다.

- 각 클래스 별로 별도 키 관리 
- 유니온 쿼리 성능 저하

이 때, 공통의 `Primary key` 를 가지고자 한다면, `Key Table` 을 별도로 운용해야 한다.

> ID TABLE 전략은 모든 데이터베이스에서 사용이 가능하지만 최적화 되어있지 않은 테이블을 사용하기 때문에 성능 문제에 이슈가 있다.

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // 상속 구현 전략
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@TableGenerator(name = "PRODUCT_SEQ_GENERATOR",
	table = "PRODUCT_SEQUENCES",
	pkColumnValue = "PRODUCT_SEQ",
	allocationSize = 1)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Product implements Serializable {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE,
		generator = "PRODUCT_SEQ_GENERATOR")
	private String id;
    
    // ...
}
```

- @TableGenerator
- @GeneratedValue

장점으로는 객체 관점에서 상위 엔티티 클래스엔 일반화된 도메인 로직과 필드를 구성할 수 있다는 점이다.

데이터 베이스 관점에선 각 구현 엔티티 클래스에서 공통적인 컬럼을 지닌다는 장점과 각 구현 클래스 별로 ID 를 별도로 관리하기 때문에 구현 클래스를 직접적으로 사용할 경우 효과적인 전략이다.(조인, 조건을 붙일 필요도 없이 특정 테이블에 대해 요청만 하면 된다.)

단점으로는 구현 클래스들을 상위 클래스로서 사용하고자 할 때, `Union` 를 사용해서 조회 쿼리를 구현해야 한다. 또한, `Key Table`을 관리할 경우엔 조회 쿼리가 더 복잡해질 수 있다.

```sql
CREATE TABLE UserProduct
(
    id     BIGINT       NOT NULL,
    name   VARCHAR(255),
    type   VARCHAR(255),
    gender VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CompanyProduct
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    type VARCHAR(255),
    PRIMARY KEY (id)
);
```

## 트레이드 오프

올바른 상속 전략을 선택하기 위해선, 각 상속 전략에 대한 장단점을 인지하고 현재 애플리케이션에 수용할 수 있는지에 대해 고려해야 한다.

- Single Table
  - 최고의 조회 성능이 필요하고 다형성 쿼리 및 관계를 사용해야 하는 경우 
  - 데이터 불일치의 위험을 증가시키는 하위 클래스 속성에 제약 조건(`not null` 또는 인덱스 등)을 사용할 수 없다는 점에 유의
- Joined Table
  - 데이터 일관성이 성능보다 더 중요하고 다형성 쿼리 및 관계가 필요한 경우
- Table per Class
  - 다형성 쿼리나 관계가 필요하지 않은 경우 
  - 제약 조건을 사용하여 데이터 일관성을 보장하고, 다형성 쿼리 옵션을 제공 
  - 그러나 다형성 쿼리는 테이블 구조 특성상 매우 복잡해 질 가능성이 높다.

## Reference

- [Baeldung - Hibernate inheritance](https://www.baeldung.com/hibernate-inheritance)
