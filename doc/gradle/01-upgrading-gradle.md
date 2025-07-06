# Gradle
## Upgrading Gradle

이 장에서는 Gradle 8.x 버전에서 최신 Gradle 릴리즈로 업그레이드하는 절차를 안내한다.

> 만약 Gradle 4.x, 5.x, 6.x, 7.x 버전에서 마이그레이션하려면, 기존에 제공된 이전 마이그레이션 가이드를 먼저 참고해야 한다.

---

## 업그레이드 절차

1. [deprecation 경고 및 API 점검](#1-deprecation-경고-및-api-점검)
2. [플러그인 업데이트](#2-플러그인-업데이트)
3. [Gradle Wrapper 버전 업데이트](#3-gradle-wrapper-버전-업데이트)
4. [프로젝트 실행 및 문제 해결](#4-프로젝트-실행-및-문제-해결)

### 1. deprecation 경고 및 API 점검

빌드 시 deprecations(사용 중단 예정 기능) 경고를 미리 확인하고 준비해야 한다.

- `gradle help --scan`
- `gradle help --warning-mode=all`

#### `gradle help --scan`: 빌드 스캔을 수행해 deprecation 경고 확인

`gradle help --scan`을 실행하면,

**빌드 스캔** 결과에서 deprecations 뷰를 확인할 수 있다. 이 화면에서 실제로 내 빌드에 적용되는 경고 및 향후 제거될 기능 사용 여부를 손쉽게 볼 수 있다.

#### `gradle help --warning-mode=all`: 콘솔에서 deprecation 경고 직접 확인

만약 빌드 스캔을 사용하지 않고 싶다면, 

`gradle help --warning-mode=all` 명령을 실행하면 콘솔에 모든 deprecation 경고가 출력된다. 다만, 빌드 스캔만큼 상세하진 않을 수 있다.

### 2. 플러그인 업데이트

일부 Gradle 플러그인은 `Gradle`의 내부 API를 사용하기 때문에, 최신 버전에서는 호환성 문제가 생길 수 있다.

위의 단계(빌드 스캔이나 warning 출력)를 통해 어떤 플러그인 또는 스크립트가 `deprecated API`를 사용하는지 미리 파악하고, **관련 플러그인을 반드시 최신 버전으로 업데이트해야 한다.**

### 3. Gradle Wrapper 버전 업데이트

`gradle wrapper --gradle-version 8.14.3` 명령어를 실행해 프로젝트를 Gradle 8.14.3 버전으로 업그레이드한다.

### 4. 프로젝트 실행 및 문제 해결

업그레이드 후, 프로젝트를 실제로 실행해보고 오류가 발생한다면 [Troubleshooting Guide](https://docs.gradle.org/current/userguide/userguide.html#troubleshooting)에서 안내하는 대로 문제를 해결한다.

---

## Reference

- https://docs.gradle.org/current/userguide/upgrading_version_8.html
