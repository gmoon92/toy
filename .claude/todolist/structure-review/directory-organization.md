# 디렉토리 구조 개선 검토

## 현재 구조

```
.claude/docs/
├── claude-code-meta/       # 프론트매터 규격
├── external-reference/     # 외부 참조 자료
├── feedback/              # 피드백 문서
├── plans/                 # 계획 문서
├── prompts/               # 프롬프트 템플릿
├── reverse-engineering/   # 역설계 가이드
├── skill-building/        # 스킬 개발 가이드
└── todolist/              # 개선 작업 목록 (신규)
```

## 검토 사항

### 1. Naming Convention
- [ ] 모든 디렉토리명이 kebab-case인지 확인
- [ ] 일관된 네이밍 패턴 적용

### 2. README Coverage
- [ ] 각 디렉토리에 README.md가 있는지 확인
- [ ] README에 디렉토리 목적과 구조 설명
- [ ] cross-reference 링크 추가

### 3. 문서 간 연계
- [ ] 관련 문서 간 링크 연결
- [ ] 상위 개념 → 하위 개념 흐름 확인
- [ ] 중복 내용 통합

### 4. 파일명 정리
- [ ] 일관된 번호 체계 (01-, 02- 등)
- [ ] 의미 있는 파일명 사용
- [ ] 불필요한 중복 단어 제거
