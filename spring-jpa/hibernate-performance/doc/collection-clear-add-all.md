# JPA Collection 관리

# Collection add all

### should be mapped with insert="false" update="false"

다중컬럼을 복합키 사용으로 

`insertable = false, updatable = false` 추가

```text
Caused by: org.hibernate.MappingException: Repeated column in mapping for entity: com.gmoon.hibernateperformance.team.domain.TeamMember column: member_id (should be mapped with insert="false" update="false")
	at org.hibernate.mapping.PersistentClass.checkColumnDuplication(PersistentClass.java:862)
	at org.hibernate.mapping.PersistentClass.checkPropertyColumnDuplication(PersistentClass.java:880)
	at org.hibernate.mapping.PersistentClass.checkColumnDuplication(PersistentClass.java:902)
	at org.hibernate.mapping.PersistentClass.validate(PersistentClass.java:634)
	at org.hibernate.mapping.RootClass.validate(RootClass.java:267)
	at org.hibernate.boot.internal.MetadataImpl.validate(MetadataImpl.java:354)
	at org.hibernate.boot.internal.SessionFactoryBuilderImpl.build(SessionFactoryBuilderImpl.java:465)
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.build(EntityManagerFactoryBuilderImpl.java:1259)
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:58)
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:365)
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:409)
	... 64 more
```



```text
javax.persistence.EntityExistsException: A different object with the same identifier value was already associated with the session : [com.ahea.core.domain.WebConsoleBannerTarget#WebConsoleBannerTarget.Id(webConsoleBannerId=1000000zwb00000003, partnerId=100000000p00000001)]

	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:123)
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:181)
	at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:188)
	at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1367)
	at org.hibernate.internal.SessionImpl.flush(SessionImpl.java:1350)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:314)
	at com.sun.proxy.$Proxy178.flush(Unknown Source)
	at com.ahea.core.base.BaseRepositoryTestCase.flushAndClear(BaseRepositoryTestCase.java:22)
	at com.ahea.core.repository.WebConsoleBannerRepositoryJpaTest.tearDown(WebConsoleBannerRepositoryJpaTest.java:132)
```

### TransientPropertyValueException 발생
### Not-null property references a transient value

Embedded 객체에 ID 값이 없는 도메인일 경우 발생

```java
Caused by:org.hibernate.TransientPropertyValueException:Not-null
property references
a transient value -
transient instance must
be saved
before current
operation :com.gmoon.hibernateperformance.team.domain.TeamMember.member ->com.gmoon.hibernateperformance.member.domain.Member
at org.hibernate.action.internal.UnresolvedEntityInsertActions.

checkNoUnresolvedActionsAfterOperation(UnresolvedEntityInsertActions.java:122)

at org.hibernate.engine.spi.ActionQueue.

checkNoUnresolvedActionsAfterOperation(ActionQueue.java:436)

at org.hibernate.internal.SessionImpl.

checkNoUnresolvedActionsAfterOperation(SessionImpl.java:587)

at org.hibernate.internal.SessionImpl.

firePersist(SessionImpl.java:730)
	...92more
```

### NULL not allowed for column


```java
Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: NULL not allowed for column "TEAM_ID"; SQL statement:
/* insert com.gmoon.hibernateperformance.team.domain.TeamMember */ insert into team_member (member_id, team_id) values (?, ?) [23502-200]
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:459)
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:429)
	at org.h2.message.DbException.get(DbException.java:205)
	at org.h2.message.DbException.get(DbException.java:181)
	at org.h2.table.Column.validateConvertUpdateSequence(Column.java:374)
	at org.h2.table.Table.validateConvertUpdateSequence(Table.java:845)
	at org.h2.command.dml.Insert.insertRows(Insert.java:187)
	at org.h2.command.dml.Insert.update(Insert.java:151)
	at org.h2.command.CommandContainer.update(CommandContainer.java:198)
	at org.h2.command.Command.executeUpdate(Command.java:251)
	at org.h2.jdbc.JdbcPreparedStatement.executeUpdateInternal(JdbcPreparedStatement.java:191)
	at org.h2.jdbc.JdbcPreparedStatement.executeUpdate(JdbcPreparedStatement.java:152)
	at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:197)
	... 90 more
```

- https://stackoverflow.com/questions/45411890/jpa-how-to-cascade-save-entities-when-child-has-embedded-id

---

케스케이드 옵션으로 저장시 이미 도메인에 아이디가 있을경우 발생

```java
class TeamRepositoryTest {
    @Test
    void collectionSaveAll() {
        List<Member> savedMembers = memberRepository.saveAll(
                Arrays.asList(
                        Member.newInstance("gmoon"),
                        Member.newInstance("anonymousUser1")
                ));
    
        Team web1 = Team.newInstance("web1");
        web1.addMembers(savedMembers);
        teamRepository.save(web1);
        log.info("Database data setup done...");
    }
}
```


@Transactional 추가로 해결

```text
org.springframework.dao.InvalidDataAccessApiUsageException: detached entity passed to persist: com.gmoon.hibernateperformance.member.domain.Member; nested exception is org.hibernate.PersistentObjectException: detached entity passed to persist: com.gmoon.hibernateperformance.member.domain.Member
```

- https://delf-lee.github.io/post/detached-entity-passed-to-persist-error/
