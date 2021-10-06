## 12. IoC 컨테이너 9부: ResourceLoader

리소스를 읽어오는 기능을 제공하는 인터페이스

ApplicationContext extends [ResourceLoader](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/ResourceLoader.html)

### 리소스 읽어오기

- 파일 시스템에서 읽어오기
- 클래스패스에서 읽어오기
- URL로 읽어오기
- 상대/절대 경로로 읽어오기

Resource getResource(java.lang.String location)

자세한건 다음에 이어질 [Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html) 추상화 시간에 자세히 다루겠습니다.

## 13. Resource 추상화

org.springframework.core.io.Resource

### 특징

- java.net.URL을 추상화 한 것.
- 스프링 내부에서 많이 사용하는 인터페이스.

### 추상화 한 이유

- 클래스패스 기준으로 리소스 읽어오는 기능 부재
- ServletContext를 기준으로 상대 경로로 읽어오는 기능 부재
- 새로운 핸들러를 등록하여 특별한 URL 접미사를 만들어 사용할 수는 있지만 구현이 복잡하고 편의성 메소드가 부족하다.

### [인터페이스 둘러보기](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html)

- 상속 받은 인터페이스
- 주요 메소드 
  - getInputStream() 
  - exitst() 
  - isOpen() 
  - getDescription(): 전체 경로 포함한 파일 이름 또는 실제 URL

### 구현체

- UrlResource: [java.net.URL](https://docs.oracle.com/javase/7/docs/api/java/net/URL.html) 참고, 기본으로 지원하는 프로토콜 http, https, ftp, file, jar.
- ClassPathResource: 지원하는 접두어 classpath:
- FileSystemResource
- ServletContextResource: 웹 애플리케이션 루트에서 상대 경로로 리소스 찾는다.
- ...

### 리소스 읽어오기

- Resource의 타입은 locaion 문자열과 ApplicationContext의의 타입에 따라 결정 된다.
  - ClassPathXmlApplicationContext -> ClassPathResource 
  - FileSystemXmlApplicationContext -> FileSystemResource 
  - WebApplicationContext -> ServletContextResource
- ApplicationContext의의 타입에 상관없이 리소스 타입을 강제하려면 java.net.URL 접두어(+접두어 classpath:)중중 하나를 사용할 수 있다.있다
  - classpath:me/whiteship/config.xml -> ClassPathResource 
  - file:///some/resource/path/config.xml -> FileSystemResource