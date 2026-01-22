## 문제 해결 가이드

### IntelliJ에서 "Compile classpath for source set 'main'" 경고

**증상:**

```
Compile classpath for source set 'main' in module 'platform' is empty
```

**원인:**

- 집합 모듈에 Spring Boot 플러그인이 적용됨
- `src/main/java` 디렉토리가 없음
- IntelliJ가 소스 세트를 찾으려고 시도

**해결:**

```kotlin
// Before (패턴 2)
plugins {
    id("org.springframework.boot")  // 적용됨
}
tasks.named<BootJar>("bootJar") {
    enabled = false
}

// After (패턴 1)
plugins {
    id("org.springframework.boot") apply false  // 선언만
}
// bootJar 설정 불필요
```

---

### Kotlin DSL에서 bootJar Unresolved reference 에러

**증상:**

```
Unresolved reference: bootJar
Unresolved reference: enabled
```

**원인:**

- import 문 누락
- tasks 컨텍스트 없이 직접 호출
- 타입 정보 부족

**해결:**

```kotlin
// Before (에러 발생)
tasks.bootJar {
    enabled = false
}

// After (올바른 방법)
import org . springframework . boot . gradle . tasks . bundling . BootJar

        tasks.named<BootJar>("bootJar") {
            enabled = false
        }
```

---

### 하위 모듈에서 spring-boot-starter 버전 찾지 못함

**증상:**

```
Could not find org.springframework.boot:spring-boot-starter:.
Required by: project :platform:platform-auth:auth-server
```

**원인:**

- 루트의 `subprojects` 블록이 의존성 추가
- 집합 모듈에 플러그인 미적용으로 버전 정보 없음

**해결 1: 패턴 2 사용**

```kotlin
// platform/build.gradle.kts
plugins {
    id("org.springframework.boot")  // 플러그인 적용
    id("io.spring.dependency-management")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}
```

**해결 2: 패턴 1의 subprojects에서 플러그인 적용**

```kotlin
// platform/platform-auth/build.gradle.kts
plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

subprojects {
    apply(plugin = "org.springframework.boot")  // 여기서 적용
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
    }
}
```

---

### 라이브러리 모듈이 실행 가능한 jar로 빌드됨

**증상:**

```
auth-client 모듈이 bootJar로 빌드되어 다른 모듈에서 의존성으로 사용 불가
```

**원인:**

- 라이브러리 모듈에 `bootJar` 비활성화 설정 누락

**해결:**

```kotlin
// auth-client/build.gradle.kts
import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
```

---

### 집합 모듈이 불필요한 태스크를 포함

**증상:**

```
./gradlew platform:bootRun
Task 'bootRun' not found in project ':platform'
```

**원인:**

- 패턴 2 사용으로 Spring Boot 플러그인이 직접 적용됨
- 집합 모듈에 불필요한 태스크 생성

**해결:**

```kotlin
// 패턴 1로 변경
plugins {
    id("org.springframework.boot") apply false  // 직접 적용 안함
}
```

---
