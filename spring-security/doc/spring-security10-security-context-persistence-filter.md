# SecurityContextPersistenceFilter

SecurityContext 영속화 필터 

SecurityContextRepository를 사용해서 기존의 SecurityContext를 읽어오거나 초기화 한다.

- 기본으로 사용하는 전략은 HTTP Session을 사용한다.
- Spring-Session과 연동하여 세션 클러스터를 구현할 수 있다. (이 강좌에서는 다루지 않습니다.)

SecurityContextPersistenceFilter는 필터 체인에서 두 번째로 동작하는 필터이다.

1. WebAsyncManagerIntegrationFilter
2. SecurityContextPersistenceFilter

SecurityContextRepository 에 위임을 해서 SecurityContext 를 가져온다.

SecurityContextRepository의 기본 전략 구현체는 `HttpSessionSecurityContextRepository` 이다.

해당 필터는 인증 정보를 Http Session 을 사용하여 세션에 로그인한 사용자 정보를 담고 있는다.

Spring Security 는  Principal 를 담고 관리하기 위해 해당 필터는 어느 필터보다 우선으로 실행되어야 한다.

```text
# SecurityContextPersistenceFilter.class

This filter MUST be executed BEFORE any authentication processing mechanisms.
Authentication processing mechanisms (e.g. BASIC, CAS processing filters etc) expect
the <code>SecurityContextHolder</code> to contain a valid <code>SecurityContext</code>
by the time they execute.
```

만약 인증과 관련된 커스텀한 인증 필터를 만든다고 하자면 SecurityContextPersistenceFilter 이후에 실행되도록 설정해야 한다. 



 
