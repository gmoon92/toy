## 8. IoC 컨테이너 6부: Environment 1부. 프로파일

프로파일과 프로퍼티를 다루는 인터페이스.

ApplicationContext extends EnvironmentCapable - getEnvironment()

### 프로파일

- 빈들의 그룹
- `Environment`의 역할은 활성화할 프로파일 확인 및 설정

### 프로파일 유즈케이스

- 테스트 환경에서는 A라는 빈을 사용하고, 배포 환경에서는 B라는 빈을 쓰고 싶다.
- 이 빈은 모니터링 용도니까 테스트할 때는 필요가 없고 배포할 때만 등록이 되면 좋겠다.

### 프로파일 정의하기

- 클래스에 정의 
  - @Configuration @Profile(“test”) 
  - @Component @Profile(“test”)
- 메소드에 정의 
  - @Bean @Profile(“test”)

### 프로파일 설정하기

- -Dspring.profiles.avtive=”test,A,B,...”
- @ActiveProfiles (테스트용)

### 프로파일 표현식

- ! (not)
- & (and)
- | (or)