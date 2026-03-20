좋다. 이건 단순 “읽힌다/안 읽힌다” 수준이 아니라 **Claude Code의 메모리 로딩 전략 (hierarchical + lazy loading)**을 정확히 이해해야 하는 영역이다.
공식 문서 + 실제 동작 기준으로 **모노레포 상황**을 전제로 정리해준다.

---

# 1. 핵심 결론 (먼저 요약)

모노레포에서 CLAUDE.md는 다음 규칙으로 로딩된다:

### ✅ 1) 실행 위치 기준 “위쪽(부모)”은 항상 로딩됨

* 현재 작업 디렉토리 → 루트까지 올라가며 모두 로딩

### ✅ 2) “아래쪽(서브 모듈)”은 자동 로딩되지 않음

* **해당 디렉토리 파일을 실제로 읽을 때만 lazy loading**

### ✅ 3) 루트에서 실행하면

* 루트 CLAUDE.md는 무조건 로딩
* 각 모듈 CLAUDE.md는 **필요할 때만 선택적으로 로딩**

### ✅ 4) 모듈에서 실행하면

* 모듈 CLAUDE.md + 상위(root) CLAUDE.md **모두 로딩됨**

---

# 2. 공식 동작 원리 (문서 기반)

Claude Code는 CLAUDE.md를 이렇게 찾는다:

> “현재 디렉토리에서 시작해서 부모 디렉토리로 올라가며 CLAUDE.md를 읽는다” ([Claude API Docs][1])

그리고 추가 규칙:

> “하위 디렉토리 CLAUDE.md는 시작 시 로드되지 않고, 해당 디렉토리를 읽을 때만 로드된다” ([Claude API Docs][1])

즉:

```
(상위) ← eager loading
(하위) ← lazy loading
```

---

# 3. 모노레포 구조 기준 분석

예시:

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

---

# 4. 케이스별 동작 분석

## ✅ 케이스 1: 루트에서 Claude 실행

```
cd repo
claude
```

### 초기 로딩

* ✅ `repo/CLAUDE.md` → 로딩됨
* ❌ `backend/CLAUDE.md` → 아직 안 로딩
* ❌ `frontend/CLAUDE.md` → 아직 안 로딩

👉 이유:

* 하위 디렉토리는 lazy loading

---

### 이후 행동에 따른 변화

#### 1) backend 코드 읽기 시작

```
backend/src/service.ts 열어줘
```

👉 이 순간:

* ✅ `backend/CLAUDE.md` 로딩됨

#### 2) frontend 작업 시작

```
frontend/App.tsx 수정해줘
```

👉 이 순간:

* ✅ `frontend/CLAUDE.md` 로딩됨

---

### ✔️ 특징

* 루트 실행 = **global context + 필요한 모듈만 점진 로딩**
* 모노레포에서 가장 일반적인 방식

---

## ✅ 케이스 2: 특정 모듈에서 Claude 실행

```
cd repo/backend
claude
```

### 초기 로딩

* ✅ `backend/CLAUDE.md`
* ✅ `repo/CLAUDE.md`

👉 이유:

* “현재 디렉토리 → 부모 방향 recursive 로딩” ([Claude API Docs][1])

---

### ✔️ 특징

* backend 전용 context + root 정책 같이 적용
* frontend 내용은 전혀 로딩 안 됨

---

# 5. 동작을 한 줄로 정리하면

```
현재 위치 기준:
- 위쪽: 항상 로딩 (hierarchical)
- 아래쪽: 필요할 때만 로딩 (lazy)
```

---

# 6. 우선순위 (충돌 시)

Claude는 “더 구체적인 것”이 이긴다:

* global (~/.claude)
* root CLAUDE.md
* module CLAUDE.md

👉 module이 root override 가능 ([Serenities AI][2])

---

# 7. 중요한 실무 포인트 (모노레포 설계)

## 🔥 1) 루트 CLAUDE.md 역할

* 공통 규칙
* 조직 표준
* 공통 툴/컨벤션

## 🔥 2) 모듈 CLAUDE.md 역할

* 도메인 규칙
* 프레임워크 특화
* 빌드/테스트 방식

---

## 🔥 3) 잘못된 설계 패턴

### ❌ 루트에 모든 걸 몰아넣기

* 컨텍스트 낭비
* relevance 떨어짐

### ❌ 모듈에만 작성

* 루트 실행 시 적용 안 됨 (초기)

---

## 🔥 4) 권장 패턴 (모노레포)

```
root CLAUDE.md
  → 공통 규칙 (항상 로딩)

각 module CLAUDE.md
  → 해당 코드 접근 시 자동 로딩
```

---

# 8. 오해하기 쉬운 포인트

## ❗ 오해 1: “모든 CLAUDE.md가 항상 로딩된다”

→ ❌ 틀림
→ 하위는 lazy loading

---

## ❗ 오해 2: “루트에서 실행하면 모든 모듈 rules 적용된다”

→ ❌ 틀림
→ 해당 모듈 파일 접근 전까지는 적용 안 됨

---

## ❗ 오해 3: “모듈 CLAUDE.md는 독립적이다”

→ ❌ 틀림
→ 항상 root 영향을 받음 (inherit 구조)

---

# 9. CLI 기준 실제 체감 동작

| 실행 위치         | 로딩되는 CLAUDE.md  |
| ------------- | --------------- |
| repo/         | root            |
| repo/backend  | backend + root  |
| repo/frontend | frontend + root |

그리고:

| 작업 대상          | 추가 로딩              |
| -------------- | ------------------ |
| backend 파일 읽음  | backend CLAUDE.md  |
| frontend 파일 읽음 | frontend CLAUDE.md |

---

# 10. 결론 (아키텍처 관점)

모노레포에서 CLAUDE.md는 사실상:

### 👉 “디렉토리 스코프 기반 컨텍스트 시스템”

* 루트 = global policy layer
* 모듈 = domain-specific layer
* 로딩 방식 = **progressive disclosure (lazy loading)**

---

# 11. 한 줄 핵심 요약

👉 **“Claude는 현재 위치 기준으로 위는 항상, 아래는 필요할 때만 읽는다.”**

---

원하면
👉 “모노레포 CLAUDE.md 설계 템플릿 (실제 적용용)”
👉 “.claude/rules vs CLAUDE.md 구조 비교”

까지 이어서 더 깊게 설계 관점으로 정리해줄게.

[1]: https://docs.anthropic.com/en/docs/claude-code/memory?utm_source=chatgpt.com "Manage Claude's memory - Anthropic"
[2]: https://serenitiesai.com/articles/claude-md-complete-guide-2026?utm_source=chatgpt.com "CLAUDE.md Guide: Configure Claude Code Like a Pro (2026) | Serenities AI"
