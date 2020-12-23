# 학습 목표

1. OneToOne 성능 향상 방법을 생각해보자.

Member - MemberOption 도메인은 OneToOne 양방향 관계로 설정되어 있다.
이때 Member 도메인에 MemberOption의 글로벌 패치 전략을 Lazy로 설정했음에도
Member를 조회할때, MemberOption을 조회하는 쿼리가 발생하게 된다.

결과적으로 성능상 문제가 발생할 수 있다. 어떻게 해결할 수 있을까?
     
# 1. fetchJoin vs fetchAll

1. fetchJoin : 마지막으로 정의 된 조인에 "fetchJoin" 플래그 추가
    - 컬렉션 조인(*ToMany)은 중복 행을 초래할 수 있으며, "inner join fetchJoin" 플래그(fetchJoin)를 통해 결과 집합을 제한할 수 있다.
    
2. fetchAll : 마지막으로 정의 된 조인에 "fetchJoin all properties" 플래그 추가
    - Lazy 패치 타입의 프로퍼티를 초기화 없이 가져 오도록 강제할 수 있다.

### example1. fetchJoin

``` java
queryFactory.select(qMember).from(qMember)
      .leftJoin(qMember.memberOption, qMemberOption).fetchJoin()
      .fetchOne();
```
`left join fetch` 단일 쿼리 발생
``` text
select member1 
  from Member member1 
left join fetch member1.memberOption as memberOption  
```

### example2. fetchAll

``` java
queryFactory.select(qMember) .from(qMember)
      .leftJoin(qMember.memberOption, qMemberOption).fetchAll()
      .fetchOne();
```

``` text
select member1 
  from Member member1 
left join member1.memberOption as memberOption fetch all properties
```

단일 쿼리를 발생하진 않고, Lazy 패치 전략으로 설정된 필드 값을 즉시 사용할 수 있다.

>If you are using property-level lazy fetching (with bytecode instrumentation), it is possible to force Hibernate to fetch the lazy properties in the first query immediately using fetch all properties.
>[Hibernate reference](https://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html)
   
- [Fetch All Properties not working](https://forum.hibernate.org/viewtopic.php?p=2249643)

 2. @OneToMany fetchJoin과 pagination을 같이 사용되면 발생되는 문제점
 https://bottom-to-top.tistory.com/45
 
 ---
 # TODO
 
fetchJoin의 문제점
 
- @OneToMany fetchJoin과 pagination을 같이 사용되면 발생되는 문제점 : [참고 링크](https://bottom-to-top.tistory.com/45)
  
 # 참고 
 
 - [Hibernate-QueryHql](https://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html)
 - [QueryDsl - JPAQueryBase](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/jpa/JPAQueryBase.html)
