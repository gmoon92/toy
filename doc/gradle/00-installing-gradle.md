# Gradle
# Gradle 설치하기

## TOC

- [Gradle 설치](#gradle-설치)
- [사전 요구사항](#사전-요구사항)
- [설치 방법](#설치-방법)
- [설치 확인](#설치-확인)

---

## Gradle 설치

**Gradle 프로젝트를 실행만 하려는 거라면**, 

프로젝트에 **`Gradle Wrapper`** 가 포함되어 있다면 별도로 `Gradle`을 설치할 필요가 없다.

```text
.
├── gradle
│   └── wrapper
├── gradlew       # Gradle Wrapper (Linux/macOS)
├── gradlew.bat   # Gradle Wrapper (Windows)
└── ⋮
```

- 프로젝트 루트 디렉터리
- Gradle Wrapper
- Gradle 빌드 실행용 스크립트

이처럼 프로젝트 루트 디렉터리에 `gradlew` 또는 `gradlew.bat` 파일이 있으면, [`Gradle Wrapper`](https://docs.gradle.org/current/userguide/gradle_wrapper.html#gradle_wrapper_reference)를 사용하는 것이다. 따라서 **`Gradle Wrapper`가 있다면 `Gradle`을 따로 설치하지 않아도 바로 빌드를 실행할 수 있다.**

> `Android Studio`는 자체에 `Gradle`이 내장되어 있기 때문에 별도의 Gradle 설치가 필요 없다.
 
---

## Gradle 버전 업그레이드

프로젝트의 Gradle 버전을 업그레이드할 때는 

- [공식 가이드의 Gradle 업그레이드 절차](https://docs.gradle.org/current/userguide/upgrading_version_8.html#upgrading_version_8)를 참고하고,
- **반드시 항상 `Gradle Wrapper`를 이용해 업그레이드해야 한다.**

모든 Gradle 릴리즈와 체크섬 정보는 [공식 릴리즈 페이지](https://gradle.org/releases/)에서 확인할 수 있다.

---

## 사전 요구사항

`Gradle`은 JDK 8 이상이 필요하며, 주요 운영 체제에서 모두 동작한다.

> 더 자세한 호환성 정보는 공식 [호환성 매트릭스](https://docs.gradle.org/current/userguide/compatibility.html#compatibility)에서 확인하자.

`Gradle`은 아래 3가지 경로로 JDK를 찾는다.

- 시스템의 PATH에 등록된 JDK
- IDE에서 사용하는 JDK
- 프로젝트에 명시적으로 지정한 JDK

예를 들어, `$PATH`가 JDK17을 가리키는 경우:

```bash
❯ echo $PATH
/opt/homebrew/opt/openjdk@17/bin
```

여러 JDK가 설치된 환경에서는 `JAVA_HOME` 환경 변수를 지정해 특정 JDK 경로로 설정할 수도 있다:

```plaintext
❯ echo %JAVA_HOME%
C:\Program Files\Java\jdk1.7.0_80

❯ echo $JAVA_HOME
/Library/Java/JavaVirtualMachines/jdk-16.jdk/Contents/Home
```

`Gradle`은 빌드 언어로 `Kotlin`과 `Groovy`를 지원한다.
`Gradle`에는 자체적으로 필요한 `Kotlin`과 `Groovy` 라이브러리가 포함되어 있어서 별도 설치가 필요 없으며, 시스템에 이미 설치된 버전은 무시된다.

자바, 그루비, 코틀린, 안드로이드 관련 전체 호환성 참고 사항은 공식 문서에서 확인할 수 있다.

---

## 설치 확인

직접 로컬에 `Gradle`을 설치하려면 먼저 `Gradle`이 설치되어 있는지 확인한다.

콘솔(혹은 Windows 명령 프롬프트)에서 `gradle -v`를 실행하면 `Gradle`이 정상적으로 동작하는지와 버전 정보를 확인할 수 있다.

```bash
❯ gradle -v

------------------------------------------------------------
Gradle 8.14.3
------------------------------------------------------------

Build time:    2024-06-17 18:10:00 UTC
Revision:      6028379bb5a8512d0b2c1be6403543b79825ef08

Kotlin:        1.9.23
Groovy:        3.0.21
Ant:           Apache Ant(TM) version 1.10.13 compiled on January 4 2023
Launcher JVM:  11.0.23 (Eclipse Adoptium 11.0.23+9)
Daemon JVM:    /Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home (no JDK specified, using current Java home)
OS:            Mac OS X 14.5 aarch64
```

> 아무 결과도 나오지 않는다면 `Gradle`이 설치되어 있지 않은 것이고, 아래 안내에 따라 설치를 진행하면 된다.

Gradle 배포 파일의 무결성을 확인하려면 릴리즈 페이지에서 SHA-256 파일을 받아 공식 안내에 따라 검증할 수 있다.

---

## 설치 방법

`Gradle`은 Linux, macOS, Windows 환경에서 설치할 수 있다.

설치 방법은 수동 설치, 또는 `SDKMAN!`, `Homebrew`와 같은 패키지 매니저를 이용하는 두 가지 방식이 있다.

- [Linux 설치: 패키지 매니저 또는 수동 설치 방법](https://docs.gradle.org/current/userguide/installation.html#linux_installation)
- [macOS 설치: 패키지 매니저 또는 수동 설치 방법](https://docs.gradle.org/current/userguide/installation.html#macos_installation)
- [Windows 설치: 수동 설치 방법](https://docs.gradle.org/current/userguide/installation.html#windows_installation)

더 자세한 설치 절차와 명령어는 각 링크에서 확인할 수 있다.

---

## Reference

- https://docs.gradle.org/current/userguide/installation.html#macos_installation
- https://docs.gradle.org/current/userguide/gradle_wrapper.html#gradle_wrapper_reference
