# CLAUDE.md 검증 체크리스트 (Validation Checklist)

## 1. 프론트 매터 검증 (특수 케이스)

### CLAUDE.md는 프론트 매터가 없음

- [ ] YAML 프론트 매터 (`---`로 시작하는 헤더)가 **없는지** 확인
- [ ] 파일이 순수 마크다운으로만 구성되어 있는지 확인

---

## 2. 문서 구조 검증

### 필수 섹션 (프로젝트 전체 CLAUDE.md)

- [ ] Project Overview (또는 유사한 개요)
- [ ] Directory Structure (또는 프로젝트 구조)
- [ ] Common Development Commands (또는 개발 명령어)

### 모듈별 CLAUDE.md 필수 섹션

- [ ] Repository Guidelines / Module Overview
- [ ] Project Structure & Module Organization
- [ ] Build, Test, and Development Commands
- [ ] Coding Style & Naming Conventions

### 권장 섹션

- [ ] Git Commit Conventions
- [ ] Testing Guidelines
- [ ] Architecture / Design Patterns
- [ ] Agent/Skill 사용 가이드 (해당 모듈에 적용되는 경우)

---

## 3. 내용 품질 검증

### 프로젝트 전체 CLAUDE.md

- [ ] 모든 모듈(be/, fe/, mobile/, mac/, win/)이 언급되었는지
- [ ] 각 모듈의 핵심 기술 스택이 간결하게 설명되었는지
- [ ] 디렉토리 구조가 실제와 일치하는지
- [ ] 개발 명령어가 실제로 동작하는지 (빌드, 테스트, 실행)

### 모듈별 CLAUDE.md

- [ ] 해당 모듈의 기술 스택이 명확히 기술되었는지
- [ ] 프로젝트 구조가 실제 디렉토리와 일치하는지
- [ ] 테스트 커버리지 임계값이 명시되어 있는지 (있는 경우)
- [ ] 커밋/PR 가이드라인이 포함되어 있는지

---

## 4. 중복 및 일관성 검증

### 루트 vs 모듈 CLAUDE.md 중복 체크

- [ ] 모듈별 CLAUDE.md에 루트 CLAUDE.md와 중복되는 내용이 없는지
- [ ] 중복 섹션이 있다면 루트에서 제거하고 모듈로 이동 권장

### 일관성 검사

- [ ] 명령어 예시가 해당 모듈의 실제 스크립트와 일치하는지
- [ ] 파일/디렉토리 경로가 실제 존재하는지
- [ ] 버전 정보(Flutter, Java, Node 등)가 현재와 일치하는지

---

## 5. 가독성 및 유지보수성

- [ ] 문서 길이가 150줄 이하인지 (권장)
- [ ] 코드 블록이 적절히 사용되었는지
- [ ] 테이블이 복잡하지 않은지 (가독성)
- [ ] 불필요한 ASCII 아트 다이어그램이 없는지

---

## 6. 특수 섹션 검증 (있는 경우)

### Agent/Skill 가이드 섹션

- [ ] 4단계 워크플로우가 명확히 설명되었는지 (PRD → 설계 → 구현 → 검증)
- [ ] 사용 가능한 에이전트/스킬 목록이 최신인지
- [ ] Slash command 예시가 정확한지

### 다이어그램/플로우차트

- [ ] ASCII 다이어그램이 필요한지 (텍스트로 충분하면 제거 권장)
- [ ] 다이어그램이 실제 워크플로우와 일치하는지
