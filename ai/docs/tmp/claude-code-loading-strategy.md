# Claude Code CLAUDE.md 로딩 전략

CLAUDE.md 파일이 언제, 어떻게 로딩되는지 이해하는 것은 Claude Code를 효과적으로 사용하는 핵심입니다.

---

## 핵심 원칙

Claude Code는 **계층적(hierarchical) + 지연(lazy) 로딩 전략**을 사용합니다.

| 방향 | 로딩 방식 | 설명 |
|:---|:---|:---|
| 위쪽(부모) | Eager Loading | 현재 작업 디렉토리 → 루트까지 올라가며 모두 로딩 |
| 아래쪽(서브 모듈) | Lazy Loading | 해당 디렉토리 파일을 실제로 읽을 때만 로딩 |

---

## 모노레포 구조 기준 동작

예시 구조:

```
repo/
 ├── CLAUDE.md                (root)
 ├── backend/
 │    └── CLAUDE.md
 ├── frontend/
 │    └── CLAUDE.md
 └── shared/
      └── CLAUDE.md
```

### 케이스 1: 루트에서 Claude 실행

```bash
cd repo
claude
```

**초기 로딩:**
- ✅ `repo/CLAUDE.md` → 로딩됨
- ❌ `backend/CLAUDE.md` → 아직 로딩 안 됨
- ❌ `frontend/CLAUDE.md` → 아직 로딩 안 됨

**이후 파일 접근 시:**
- `backend/src/service.ts` 읽기 → ✅ `backend/CLAUDE.md` 로딩
- `frontend/App.tsx` 수정 → ✅ `frontend/CLAUDE.md` 로딩

### 케이스 2: 특정 모듈에서 Claude 실행

```bash
cd repo/backend
claude
```

**초기 로딩:**
- ✅ `backend/CLAUDE.md`
- ✅ `repo/CLAUDE.md`

**특징:**
- backend 전용 context + root 정책 같이 적용
- frontend 내용은 전혀 로딩 안 됨

---

## 우선순위 (충돌 시)

Claude는 "더 구체적인 것"이 이긴다:

```
global (~/.claude) ← root CLAUDE.md ← module CLAUDE.md
```

→ module이 root override 가능

---

## 실무 설계 패턴

### 권장 패턴

```
root CLAUDE.md
  → 공통 규칙 (항상 로딩)

각 module CLAUDE.md
  → 해당 코드 접근 시 자동 로딩
```

### 잘못된 패턴

| 패턴 | 문제 |
|:---|:---|
| 루트에 모든 것을 몰아넣기 | 컨텍스트 낭비, relevance 떨어짐 |
| 모듈에만 작성 | 루트 실행 시 적용 안 됨 (초기) |

---

## 한 줄 요약

**"Claude는 현재 위치 기준으로 위는 항상, 아래는 필요할 때만 읽는다."**

---

## 참고 자료

- [Claude Code 공식 문서 - Memory](https://docs.anthropic.com/en/docs/claude-code/memory)
- [CLAUDE.md 완벽 가이드](https://serenitiesai.com/articles/claude-md-complete-guide-2026)
