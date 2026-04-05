# Claude Code CLAUDE.md 로딩 전략

CLAUDE.md 파일이 언제, 어떻게 로딩되는지 이해하는 것은 Claude Code를 효과적으로 사용하는 핵심입니다.

---

## 1. 핵심 원칙

Claude Code는 **계층적(hierarchical) + 지연(lazy) 로딩 전략**을 사용합니다.

**한 줄 요약**: "Claude는 현재 위치 기준으로 위는 항상, 아래는 필요할 때만 읽는다."

| 방향 | 로딩 방식 | 설명 |
|:---|:---|:---|
| 위쪽(부모) | Eager Loading | 현재 작업 디렉토리 → 루트까지 올라가며 모두 로딩 |
| 아래쪽(서브 모듈) | Lazy Loading | 해당 디렉토리 파일을 실제로 읽을 때만 로딩 |

---

## 2. 로딩 메커니즘

### 2.1 위쪽(부모) - Eager Loading

현재 디렉토리에서 시작해 부모 디렉토리로 올라가며 존재하는 모든 CLAUDE.md를 읽습니다. 정확히는 루트에서 CWD 방향으로 내려오며 순서대로 로딩합니다 (CWD에 가까울수록 나중에 로딩 = 높은 우선순위).

각 디렉토리에서 체크하는 파일:
- `CLAUDE.md`
- `.claude/CLAUDE.md`
- `.claude/rules/*.md`
- `CLAUDE.local.md` (로컬 전용, gitignore 권장)

> "현재 디렉토리에서 시작해서 부모 디렉토리로 올라가며 CLAUDE.md를 읽는다" ([Claude API Docs](https://docs.anthropic.com/en/docs/claude-code/memory))

### 2.2 아래쪽(자식) - Lazy Loading

하위 디렉토리의 CLAUDE.md는 시작 시 로드되지 않고, **해당 디렉토리 내의 파일에 실제로 접근(읽기/편집)할 때** 로드됩니다. 이 시점에서 CWD와 접근 파일 사이의 모든 중간 디렉토리의 CLAUDE.md도 함께 로드됩니다.

> "하위 디렉토리 CLAUDE.md는 시작 시 로드되지 않고, 해당 디렉토리를 읽을 때만 로드된다" ([Claude API Docs](https://docs.anthropic.com/en/docs/claude-code/memory))

```
(상위) ← eager loading
(하위) ← lazy loading
```

---

## 3. 모노레포 케이스 분석

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

### 3.1 케이스 1: 루트에서 실행

```bash
cd repo
claude
```

**초기 로딩**:
- `repo/CLAUDE.md` → 로딩됨
- `backend/CLAUDE.md` → 미로딩
- `frontend/CLAUDE.md` → 미로딩

**이후 파일 접근 시**:
- `backend/src/service.ts` 읽기 → `backend/CLAUDE.md` 로딩
- `frontend/App.tsx` 수정 → `frontend/CLAUDE.md` 로딩

**특징**: 루트 실행 = **global context + 필요한 모듈만 점진 로딩**

### 3.2 케이스 2: 특정 모듈에서 실행

```bash
cd repo/backend
claude
```

**초기 로딩**:
- `backend/CLAUDE.md`
- `repo/CLAUDE.md`

**특징**: backend 전용 context + root 정책 같이 적용, frontend 내용은 전혀 로딩 안 됨

---

## 4. 우선순위와 충돌

Claude는 "더 구체적인 것"이 이긴다:

```
Managed (/etc/claude-code/CLAUDE.md) ← User (~/.claude/CLAUDE.md) ← root CLAUDE.md ← module CLAUDE.md
```

→ 나중에 로딩된 파일일수록 우선순위가 높다 (소스 코드 주석: "the latest files are highest priority")

| 우선순위 | 범위 | 경로 예시 |
|---------|------|----------|
| 1 (높음) | Module CLAUDE.md (CWD에 가장 가까운) | `repo/backend/CLAUDE.md` |
| 2 | Root CLAUDE.md | `repo/CLAUDE.md` |
| 3 | User memory | `~/.claude/CLAUDE.md` |
| 4 (낮음) | Managed memory | `/etc/claude-code/CLAUDE.md` |

---

## 5. 실무 설계 패턴

### 5.1 권장 패턴 (모노레포)

```
root CLAUDE.md
  → 공통 규칙 (항상 로딩)

각 module CLAUDE.md
  → 해당 코드 접근 시 자동 로딩
```

**역할 분리**:

| 위치 | 역할 | 예시 |
|------|------|------|
| **Root CLAUDE.md** | 공통 규칙 | 조직 표준, 공통 툴/컨벤션 |
| **Module CLAUDE.md** | 도메인 규칙 | 프레임워크 특화, 빌드/테스트 방식 |

### 5.2 잘못된 패턴

| 패턴 | 문제 |
|:---|:---|
| 루트에 모든 것을 몰아넣기 | 컨텍스트 낭비, relevance 떨어짐 |
| 모듈에만 작성 | 루트 실행 시 적용 안 됨 (초기) |

---

## 6. 오해하기 쉬운 포인트

이러한 패턴을 적용할 때 흔히 발생하는 오해는 다음과 같습니다.

### 오해 1: "모든 CLAUDE.md가 항상 로딩된다"

→ 틀림
→ 하위는 lazy loading

### 오해 2: "루트에서 실행하면 모든 모듈 rules 적용된다"

→ 틀림
→ 해당 모듈 파일 접근 전까지는 적용 안 됨

### 오해 3: "모듈 CLAUDE.md는 독립적이다"

→ 틀림
→ 항상 root 영향을 받음 (inherit 구조)

---

## 7. CLI 기준 실제 체감 동작

| 실행 위치 | 로딩되는 CLAUDE.md |
| ---------- | ----------------- |
| repo/ | root |
| repo/backend | backend + root |
| repo/frontend | frontend + root |

| 작업 대상 | 추가 로딩 |
| ----------- | --------- |
| backend 파일 읽음 | backend CLAUDE.md |
| frontend 파일 읽음 | frontend CLAUDE.md |

---

## 8. 아키텍처 관점 정리

모노레포에서 CLAUDE.md는 사실상 **"디렉토리 스코프 기반 컨텍스트 시스템"**입니다.

전체 메모리 계층 (로딩 순서 = 낮은 우선순위 → 높은 우선순위):

| 계층 | 경로 | 용도 |
|------|------|------|
| Managed | `/etc/claude-code/CLAUDE.md` | 시스템/조직 정책 (모든 사용자) |
| User | `~/.claude/CLAUDE.md` | 개인 전역 설정 (모든 프로젝트) |
| Project | `repo/CLAUDE.md`, `.claude/CLAUDE.md`, `.claude/rules/*.md` | 프로젝트 규칙 (커밋 가능) |
| Local | `repo/CLAUDE.local.md` | 개인 프로젝트 설정 (gitignore 권장) |

- **루트** = global policy layer
- **모듈** = domain-specific layer
- **로딩 방식** = progressive disclosure (lazy loading)

---

## 참고 자료

- [Claude Code 공식 문서 - Memory](https://docs.anthropic.com/en/docs/claude-code/memory)
- [CLAUDE.md 완벽 가이드](https://serenitiesai.com/articles/claude-md-complete-guide-2026)
