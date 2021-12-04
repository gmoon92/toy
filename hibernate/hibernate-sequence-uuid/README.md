# Hibernate Sequence UUID Generator

JPA의 ID 생성 전략은 @GenericGenerator 를 활용한다.

## @GenericGenerator

@GenericGenerator

- name: 유니크한 생성기 이름을 지정한다.
- strategy: 하이버네이트가 제공하는 **`org.hibernate.id.*Generator`** 클래스 또는 IdentifierGenerator 구현한 커스텀 생성기 클래스를 정의한다.
  - Generator 클래스 경로 지정 
    - org.hibernate.id.UUIDGenerator
  - 하이버네이트 Generator 약어 지정

하이버네이트의 ID 생성 전략은 `DefaultIdentifierGeneratorFactory`를 참고하자.

## Hibernate ID Generator Acronyms

하이버네이트의 Generator 약어는 다음과 같다.

- **`uuid2`**
- **`guid`**
- **`uuid`**
- **`uuid.hex`**
- **`assigned`**
- **`identity`**
- **`select`**
- **`sequence`**
- **`seqhilo`**
- **`increment`**
- **`foreign`**
- **`sequence-identity`**
- **`enhanced-sequence`**
- **`enhanced-table`**

```java
package org.hibernate.id.factory.internal;

public class DefaultIdentifierGeneratorFactory
        implements MutableIdentifierGeneratorFactory, Serializable, ServiceRegistryAwareService {
  private ConcurrentHashMap<String, Class> generatorStrategyToClassNameMap = new ConcurrentHashMap<String, Class>();

  /**
   * Constructs a new DefaultIdentifierGeneratorFactory.
   */
  @SuppressWarnings("deprecation")
  public DefaultIdentifierGeneratorFactory() {
    register("uuid2", UUIDGenerator.class);
    register("guid", GUIDGenerator.class);            // can be done with UUIDGenerator + strategy
    register("uuid", UUIDHexGenerator.class);            // "deprecated" for new use
    register("uuid.hex", UUIDHexGenerator.class);    // uuid.hex is deprecated
    register("assigned", Assigned.class);
    register("identity", IdentityGenerator.class);
    register("select", SelectGenerator.class);
    register("sequence", SequenceStyleGenerator.class);
    register("seqhilo", SequenceHiLoGenerator.class);
    register("increment", IncrementGenerator.class);
    register("foreign", ForeignGenerator.class);
    register("sequence-identity", SequenceIdentityGenerator.class);
    register("enhanced-sequence", SequenceStyleGenerator.class);
    register("enhanced-table", TableGenerator.class);
  }

  // ...
}
```

## org.hibernate.id.UUIDGenerator

```java
public class UUIDGenerator implements IdentifierGenerator, Configurable {

  public static UUIDGenerator buildSessionFactoryUniqueIdentifierGenerator() {
    final UUIDGenerator generator = new UUIDGenerator();
    generator.strategy = StandardRandomStrategy.INSTANCE;
    generator.valueTransformer = UUIDTypeDescriptor.ToStringTransformer.INSTANCE;
    return generator;
  }
}

package org.hibernate.id.uuid;

public class StandardRandomStrategy implements UUIDGenerationStrategy {
  public static final StandardRandomStrategy INSTANCE = new StandardRandomStrategy();

  @Override
  public UUID generateUUID(SharedSessionContractImplementor session) {
    return UUID.randomUUID();
  }
}
```

- StandardRandomStrategy: UUID.randomUUID()
- CustomVersionOneStrategy: uuid 동기화 목적으로 사용, 병목 현상 발생할 수 있다.

## ID Generator Class Mapping

@GenericGenerator 의 **`strategy`** 속성에 ID 생성기를 명시하면,

하이버네이트는 <br/> 
`DefaultIdentifierGeneratorFactory` 클래스의 getIdentifierGeneratorClass 메서드를 호출하여 도메인에 대한 ID 생성기를 반환한다.

```java
package org.hibernate.id.factory.internal;

public class DefaultIdentifierGeneratorFactory
        implements MutableIdentifierGeneratorFactory, Serializable, ServiceRegistryAwareService {
  @Override
  public Class getIdentifierGeneratorClass(String strategy) {
    if ("hilo".equals(strategy)) {
      throw new UnsupportedOperationException("Support for 'hilo' generator has been removed");
    }
    String resolvedStrategy = "native".equals(strategy) ?
            getDialect().getNativeIdentifierGeneratorStrategy() : strategy;

    // ID generator 클래스 유효성 검사
    Class generatorClass = generatorStrategyToClassNameMap.get(resolvedStrategy);
    
    try {
      if (generatorClass == null) {
        final ClassLoaderService cls = serviceRegistry.getService(ClassLoaderService.class);
        generatorClass = cls.classForName(resolvedStrategy);
      }
    } catch (ClassLoadingException e) {
      throw new MappingException(String.format("Could not interpret id generator strategy [%s]", strategy));
    }
    return generatorClass;
  }
}
```

## UUID ID Of Domain

```java
@Entity
public class Company {
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid2")
  @Column(length = 50)
  private String id;
}
```

```text
call next value for system-uuid2
```

## SessionFactoryImpl

하이버네이트는 `org.hibernate.Session`을 구성할 때 SessionFactoryImpl 클래스를 사용한다.

- org.hibernate.internal.SessionFactoryImpl

다음 SessionFactoryImpl 생성자를 보면 도메인의 ID 생성 전략(IdentifierGenerator)을 매핑시켜주기 위해, DefaultIdentifierGeneratorFactory 팩토리를 사용한다는걸 알 수 있다. 

````java
public class SessionFactoryImpl implements SessionFactoryImplementor {

  public SessionFactoryImpl(
          final MetadataImplementor metadata,
          SessionFactoryOptions options,
          QueryPlanCache.QueryPlanCreator queryPlanCacheFunction) {

    
    //Generators:
    this.identifierGenerators = new HashMap<>();
    metadata.getEntityBindings().stream()
            .filter(model -> !model.isInherited())
            .forEach(model -> {
              IdentifierGenerator generator = model.getIdentifier()
                      .createIdentifierGenerator(metadata.getIdentifierGeneratorFactory(), 
                              jdbcServices.getJdbcEnvironment().getDialect(),
                              settings.getDefaultCatalogName(),
                              settings.getDefaultSchemaName(),
                              (RootClass) model
                      );
            });
    // ....
  }
}
````

## Query execute error break point.

잘못된 도메인 설계로 인해 조회할 수 없는 쿼리일 경우, Dialect 에서 쿼리 예외를 발생한다.

- org.springframework.orm.jpa.AbstractEntityManagerFactoryBean
- org.springframework.orm.jpa.vendor.HibernateJpaDialect

````java
public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
  //...
}
````

## 참고

- [Hibernate Reference - GenericGenerator](https://docs.jboss.org/hibernate/orm/5.6/javadocs/org/hibernate/annotations/GenericGenerator.html)
- [Hibernate Reference - identifiers-generators-GenericGenerator](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-GenericGenerator)
- [Hibernate Reference - Generate uuids primary keys](https://thorben-janssen.com/generate-uuids-primary-keys-hibernate/)
- [Hibernate Reference - Various additional generators](https://docs.jboss.org/hibernate/core/3.6/reference/en-US/html/mapping.html#d0e5294)