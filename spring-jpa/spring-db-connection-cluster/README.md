# Spring DB Connection Clustering

- JDBC load balancing modes
- AbstractRoutingDataSource

## Environment

- spring-boot-starter:spring-boot:2.7.8
- mariadb:10.3

## 1. [Using JDBC Multi-Host Connections](https://mariadb.com/kb/en/failover-and-high-availability-with-mariadb-connector-j/)

로드 밸런싱 드라이버는 특정 DB 서버의 장애로 연결 실패시, 다른 DB 서버로 연결 시도한다.

- 읽기/쓰기 작업 분산
- 애플리케이션 수준에서 DB 커넥션에 대한 failover 지원

> MariaDB 환경에서 failover 에 대한 동작 방식은 [mariadb.com - Failover behaviour](https://mariadb.com/kb/en/failover-and-high-availability-with-mariadb-connector-j/#failover-behaviour) 를 참고하자.

```yaml
spring:
  datasource:
    url: jdbc:(mysql|mariadb):[replication:|sequential:|loadbalance:|aurora:]//[,...]/[database][?=[&=]...]
```

- `sequential`
    - MariaDB Connector/J 1.3.0 이상
    - 다중 마스터 환경에서 사용, 슬레이브에 대한 로드 밸런싱 읽기 미지원
    - URL에 정의된 호스트 순서대로 연결 시도, 첫 번째 호스트를 접속하지 못한 경우 다음 호스트로 순차적 연결 시도
    - 항상 첫 번째 호스트부터 연결 시도를 하기 때문에, 특정 마스터 서버에만 부하가 몰릴 수 있다.
    - `jdbc:mariadb:sequential:host1,host2,host3/testdb`
- `loadbalance`
    - MariaDB Connector/J 1.2.0 이상
    - 다중 마스터 환경에서 사용, 슬레이브에 대한 로드 밸런싱 읽기 미지원
    - URL에 정의된 호스트중 무작위 연결 시도(부하 분산 처리)
    - `jdbc:mariadb:loadbalance:cluster_host1:port,cluster_host2:port/testdb`
- `replication`
  - MariaDB Connector/J 1.2.0 이상
  - 마스터/슬레이브 환경에서 슬레이브에 대한 로드 밸런싱 지원
  - 슬레이브로 설정된 호스트중 무작위 연결 시도(슬레이브 부하 분산 처리)
  - `jdbc:mariadb:replication:master,slave1,slave2/testdb`

> 로드 밸런싱 드라이버는 HA 구성을 목적의 드라이버가 아니다. 따라서 마스터 서버의 장애 발생시 SPOF(Single Point of Failure)가 발생할 수 있다. [MySQL](https://dev.mysql.com/doc/connector-j/en/connector-j-multi-host-connections.html) 의 다중 서버 JDBC 설정은 다를 수 있으니 주의하자.

### 1.1. With Spring @Transaction

로드 밸런싱 설정을 하게 되면 readOnly(connection.setReadOnly(true)) 설정시 자동으로 슬레이브 디비로 연결을 시도한다.

```java
public class MyApplication {

  public void example() {
    Connection connection = DriverManager.getConnection("jdbc:mariadb:replication://master,slave/test");
    Statement stmt = connection.createStatement();

    // 마스터 서버에서 쿼리 실행
    stmt.execute("SELECT 1");

    // readOnly 설정, 슬레이브 서버에서 쿼리 실행
    connection.setReadOnly(true);
    stmt.execute("SELECT 1");
  }
}
```

Spring(3.0.1 버전 이상) @Transaction 의 readOnly 속성을 활성하게 되면, 쿼리 실행 직전 Connection.setReadOnly(true) 로 설정하여 동작된다.

```java
@Service
public class MyService {
    
	@Autowired
	private EntityManager em;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void createContacts() {
		Contact contact1 = new Contact();
		contact1.setGender("M");
		contact1.setName("JIM");
		em.persist(contact1);
	}
}
```

## 2. AbstractRoutingDataSource

org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource 는 다중 데이터베이스 환경에서 lookup key 기반으로 동적으로 DataSource 를 사용할 수 있도록 지원한다.

- 애플리케이션 수준에서 DataSource 관리
  - DB Connection 에 대한 failover 구현 필요.
  - JPA 환경에서 DB Vender 가 각각 다른 환경 EntityManager 및 트랜잭션 커스텀 설정 필요.
- 다중 DataSource 관리 및 요청 키에 따라 동적으로 DataSource 반환
- AbstractRoutingDataSource#determineCurrentLookupKey()
  - DataSource 반환 정책 구현

```java
package org.springframework.jdbc.datasource.lookup;

public abstract class AbstractRoutingDataSource extends AbstractDataSource implements InitializingBean {

	@Nullable
	private Map<Object, Object> targetDataSources; // DataSources key/value

	@Nullable
	private Object defaultTargetDataSource; // default dataSource

	@Nullable
	private Map<Object, DataSource> resolvedDataSources;

	@Nullable
	private DataSource resolvedDefaultDataSource;

	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

	public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
		this.defaultTargetDataSource = defaultTargetDataSource;
	}

	// ...

	@Override
	public void afterPropertiesSet() {
		if (this.targetDataSources == null) {
			throw new IllegalArgumentException("Property 'targetDataSources' is required");
		}
		this.resolvedDataSources = CollectionUtils.newHashMap(this.targetDataSources.size());
		this.targetDataSources.forEach((key, value) -> {
			Object lookupKey = resolveSpecifiedLookupKey(key);
			DataSource dataSource = resolveSpecifiedDataSource(value);
			this.resolvedDataSources.put(lookupKey, dataSource);
		});
		if (this.defaultTargetDataSource != null) {
			this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	// key 를 기준으로 DataSource 선택 및 반환 
	protected DataSource determineTargetDataSource() {
		Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
		Object lookupKey = determineCurrentLookupKey();
		DataSource dataSource = this.resolvedDataSources.get(lookupKey);
		if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
			dataSource = this.resolvedDefaultDataSource;
		}
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		return dataSource;
	}

	// DataSource 반환 정책 구현 
	@Nullable
	protected abstract Object determineCurrentLookupKey();
}
```

- targetDataSources
  - 전체 DataSource 데이터 관리
- determineCurrentLookupKey()
  - DataSource 반환 정책 구현 추상화 메서드
- afterPropertiesSet()
  - setup 메서드, resolvedDataSources 초기화

## 3. With Spring Data JPA

Spring Data JPA 에서 구성은 다음과 같다.

```java
@Slf4j
public class DynamicRoutingDatabaseSource extends AbstractRoutingDataSource {

	public DynamicRoutingDatabaseSource(DataSourceProperties masterDataSourceProperties, DataSourceProperties slaveDataSourceProperties) {
		DataSource masterDataSource = createDataSource(masterDataSourceProperties);
		DataSource slaveDataSource = createDataSource(slaveDataSourceProperties);
		Map<Object, Object> dataSources = new HashMap<>();
		dataSources.put(TargetDataBase.MASTER, masterDataSource);
		dataSources.put(TargetDataBase.SLAVE, slaveDataSource);

		super.setTargetDataSources(dataSources);
		super.setDefaultTargetDataSource(masterDataSource);
		super.afterPropertiesSet();
	}

	private DataSource createDataSource(DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder()
		  .type(HikariDataSource.class)
		  .build();
	}

	@Override
	protected Object determineCurrentLookupKey() {
		boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		log.info("tx readOnly: {}", readOnly);
		if (readOnly) {
			return TargetDataBase.SLAVE;
		}

		return TargetDataBase.MASTER;
	}

	private enum TargetDataBase {
		MASTER, SLAVE
	}
}

```

Spring Data JPA 는 ThreadLocal 기반으로 EntityManager 를 관리하게 되는데, 이때 트랜잭션 관리는 TransactionSynchronizationManager 에서 한다.

TransactionSynchronizationManager 를 활용하면 현재 트랜잭션의 상태를 확인할 수 있다.

### 3.1. Properties Configuration

```yaml
spring:
  datasource:
    jdbc-url-option: createDatabaseIfNotExist=true
#    EntityManager 는 데이터베이스 및 dialect 는 단 하나만 존재 설정 가능
    driver-class-name: &driverClassName org.mariadb.jdbc.Driver
    master:
      driver-class-name: *driverClassName
      url: jdbc:mariadb://localhost:3306/spring-db-connection-cluster?${spring.datasource.jdbc-url-option}
      username: root
      password: 111111
    slave:
      driver-class-name: *driverClassName
      url: jdbc:mariadb://localhost:3306/spring-db-connection-cluster?${spring.datasource.jdbc-url-option}
      username: root
      password: 111111
      read-only: true
```

### 3.2. JPA Configuration

```java
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "com.gmoon.**",
	repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class
)
public class JpaConfig {

	@Bean
	@DependsOn("routingDataSource")
	public EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer(DataSource routingDataSource) {
		return builder -> builder.dataSource(routingDataSource)
			.persistenceUnit("default");
	}

	static class DataSourceConfig {

		@Bean
		@Primary
		@DependsOn({"masterDataSourceProperties", "slaveDataSourceProperties"})
		public DataSource routingDataSource(
			DataSourceProperties masterDataSourceProperties,
			DataSourceProperties slaveDataSourceProperties
		) {
			return new LazyConnectionDataSourceProxy(new DynamicRoutingDatabaseSource(
				masterDataSourceProperties,
				slaveDataSourceProperties
			));
		}

		@Bean
		@ConfigurationProperties("spring.datasource.master")
		public DataSourceProperties masterDataSourceProperties() {
			return new DataSourceProperties();
		}

		@Bean
		@ConfigurationProperties("spring.datasource.slave")
		public DataSourceProperties slaveDataSourceProperties() {
			return new DataSourceProperties();
		}
	}
}

```

## Reference

- [A Guide to Spring AbstractRoutingDatasource](https://www.baeldung.com/spring-abstract-routing-data-source)
- [Multitenancy With Spring Data JPA](https://www.baeldung.com/multitenancy-with-spring-data-jpa)
- [Using Transactions for Read-Only Operations](https://www.baeldung.com/spring-transactions-read-only)
- [mariadb.com](https://mariadb.com/kb/en/failover-and-high-availability-with-mariadb-connector-j/)
- [dev.mysql.com - MySQL Connector/J Developer Guide](https://dev.mysql.com/doc/connector-j/en/)
    - [ConnectorJ config failover](https://dev.mysql.com/doc/connector-j/en/connector-j-config-failover.html)
    - [Replication](https://dev.mysql.com/doc/refman/8.0/en/replication.html)
    - [Replica Replication connection](https://dev.mysql.com/doc/connector-j/en/connector-j-source-replica-replication-connection.html)
    - [Configuration Properties](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html)
    - [Configuring Load Balancing with Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-j2ee-concepts-managing-load-balanced-connections.html)
    - [Configuring Server Failover for Connections Using JDBC](https://dev.mysql.com/doc/connector-j/en/connector-j-multi-host-connections.html)
- [blog - DB분산처리를 위한 sharding](https://techblog.woowahan.com/2687/)
