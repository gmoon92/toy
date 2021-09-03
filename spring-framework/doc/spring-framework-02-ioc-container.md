# IoC 컨테이너

## 1. 스프링 IoC 컨테이너와 빈

Inversion of Control: 의존 관계 주입(Dependency Injection)이라고도 하며, 어떤 객체가 사용하는 의존 객체를 직접 만들어 사용하는게 아니라,아니라 주입 받아 사용하는 방법을 말 함.

### 1.1. 스프링 IoC 컨테이너

- BeanFactory
- 애플리케이션 컴포넌트의 중앙 저장소.
- 빈 설정 소스로 부터 빈 정의를 읽어들이고, 빈을 구성하고 제공한다.

### 1.2. 빈

- 스프링 IoC 컨테이너가 관리 하는 객체.
- 장점 ○ 의존성 관리 ○ 스코프 ■ 싱글톤: 하나 ■ 프로포토타입: 매번 다른 객체 ○ 라이프사이클 인터페이스

### 1.3. [ApplicationContext](https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/context/ApplicationContext.html)

- [BeanFactory](https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/factory/BeanFactory.html)
- 메시지 소스 처리 기능 (i18n)
- 이벤트 발행 기능
- 리소스 로딩 기능
- ...
