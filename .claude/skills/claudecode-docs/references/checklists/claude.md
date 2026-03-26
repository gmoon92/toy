# CLAUDE.md 검증 체크리스트

## CLAUDE.md 특수 검증

- [ ] **프론트매터 없음** (YAML 헤더 금지)
- [ ] 순수 마크다운으로만 구성

## 문서 위치 검증

- [ ] 프로젝트 루트 또는 모듈별
- [ ] 모듈별 CLAUDE.md는 해당 모듈 루트에 위치

## 필수 섹션

### 프로젝트 전체
- [ ] Project Overview (또는 유사 개요)
- [ ] Directory Structure (또는 프로젝트 구조)
- [ ] Common Development Commands (또는 개발 명령어)

### 모듈별
- [ ] Repository Guidelines / Module Overview
- [ ] Project Structure & Module Organization
- [ ] Build, Test, and Development Commands
- [ ] Coding Style & Naming Conventions

## 중복 및 일관성 검증

- [ ] 모듈별 CLAUDE.md에 루트와 중복 내용 없음
- [ ] 명령어 예시가 실제 스크립트와 일치
- [ ] 파일/디렉토리 경로가 실제 존재

## 가독성 검증

- [ ] 문서 길이 150줄 이하 권장
- [ ] 불필요한 ASCII 아트 다이어그램 없음

---

**참조**:
- [공통 구조 검증](../common/structure.md)
- [가이드라인](../../guidelines.md)
