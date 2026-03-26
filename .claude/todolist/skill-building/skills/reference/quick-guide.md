# 빠른 가이드: 에이전트 스킬 (Quick Guide: Agent Skills)

효과적인 스킬 생성을 위한 빠른 참조입니다.

## 최소 스킬 템플릿

```markdown
---
name: my-skill-name
description: Y에 대해 물을 때 X를 수행. Z가 발생할 때 사용.
---

# 스킬 제목

## 빠른 시작

[가장 일반적인 사용 사례와 코드 예시]

## 고급
- **기능 1**: [DETAILS.md](DETAILS.md) 참조
- **기능 2**: [REFERENCE.md](REFERENCE.md) 참조
```

---

## 프론트매터 템플릿

```yaml
---
name: skill-name              # 소문자-하이픈, 최대 64자
description: |                # 최대 1024자, 무엇과 언제
  PDF 파일에서 텍스트와 테이블을 추출합니다.
  PDF 작업이나 사용자가 문서를 언급할 때 사용.
---
```

---

## 설명 공식

**패턴:** `[무엇을 하는지] + [언제 사용하는지]`

**좋은 예시:**

```yaml
description: Excel 파일을 분석하고 보고서를 생성합니다. 스프레드시트나 .xlsx 파일 작업 시 사용.
```

```yaml
description: git diff에서 커밋 메시지를 생성합니다. 사용자가 커밋 도움이나 staged 변경사항 검토를 요청할 때 사용.
```

```yaml
description: PDF에서 텍스트와 테이블을 추출합니다. 사용자가 PDF, 양식, 또는 문서 추출을 언급할 때 사용.
```

---

## 명명 패턴

### 동명사 형태 (권장)
- `processing-pdfs`
- `analyzing-spreadsheets`
- `managing-databases`
- `testing-code`

### 명사 구
- `pdf-processing`
- `spreadsheet-analysis`
- `database-management`
- `code-testing`

### 행동 중심
- `process-pdfs`
- `analyze-spreadsheets`
- `manage-databases`
- `test-code`

---

## 점진적 공개 패턴

### 기본 구조

```markdown
# SKILL.md (핵심 워크플로우)

## 기본 사용법
[필수 지시문]

## 고급 기능
- **양식**: [FORMS.md](FORMS.md)
- **API**: [REFERENCE.md](REFERENCE.md)
- **예시**: [EXAMPLES.md](EXAMPLES.md)
```

### 도메인 조직

```markdown
# SKILL.md (탐색)

## 사용 가능한 데이터셋

**재무**: 매출, 청구 → [reference/finance.md](reference/finance.md)
**영업**: 파이프라인, 거래 → [reference/sales.md](reference/sales.md)
**제품**: 사용량, 기능 → [reference/product.md](reference/product.md)
```

---

## 워크플로우 템플릿

```markdown
## [작업 이름] 워크플로우

이 체크리스트를 복사하세요:

```
진행 상황:
- [ ] 단계 1: [작업]
- [ ] 단계 2: [작업]
- [ ] 단계 3: [작업]
- [ ] 단계 4: [검증]
```

**단계 1: [작업]**

[단계 1에 대한 상세 지시문]

**단계 2: [작업]**

[단계 2에 대한 상세 지시문]

[계속...]
```

---

## 피드백 루프 패턴

```markdown
## 프로세스

1. [작업 수행]
2. **즉시 검증**: `python validate.py`
3. 검증이 실패하면:
   - 오류 메시지 검토
   - 문제 수정
   - 재검증
4. **검증 통과 후에만 진행**
5. [다음 작업]
```

---

## 템플릿 패턴

### 엄격한 템플릿

````markdown
## 출력 구조

**항상 이 정확한 템플릿 사용:**

```markdown
# [제목]

## 요약
[한 단락 개요]

## 주요 발견사항
- 데이터가 있는 발견사항 1
- 데이터가 있는 발견사항 2

## 권장사항
1. 구체적인 권장사항
2. 구체적인 권장사항
```
````

### 유연한 템플릿

````markdown
## 출력 구조

**합리적인 기본값 (필요에 따라 적응):**

```markdown
# [제목]

## 요약
[개요]

## 발견사항
[발견한 것에 따라 섹션 조정]
```

특정 분석 유형에 따라 섹션을 적응하세요.
````

---

## 예시 패턴

```markdown
## 형식 예시

**예시 1: 기능 추가**
입력: JWT 인증 추가
출력:
```
feat(auth): JWT 기반 인증 구현

로그인 엔드포인트와 토큰 검증 미들웨어 추가
```

**예시 2: 버그 수정**
입력: 보고서의 날짜 표시 수정
출력:
```
fix(reports): 날짜 형식의 시간대 변환 수정

보고서 생성 전체에서 UTC 타임스탬프 일관되게 사용
```

이 스타일을 따르세요: type(scope): 간략한 설명, 그 다음 상세한 설명.
```

---

## 스크립트 문서화

```markdown
## 유틸리티 스크립트

**analyze_form.py**: PDF에서 모든 양식 필드 추출

```bash
python scripts/analyze_form.py input.pdf > fields.json
```

출력 형식:
```json
{
  "field_name": {"type": "text", "x": 100, "y": 200}
}
```

**validate.py**: 겹치는 경계 확인

```bash
python scripts/validate.py fields.json
# 반환: "OK" 또는 충돌 목록
```
```

---

## 자유 수준

### 높은 자유 (텍스트 안내)

```markdown
## 코드 리뷰

1. 구조와 조직 분석
2. 버그나 엣지 케이스 확인
3. 가독성 개선 제안
4. 프로젝트 규칙 검증
```

### 중간 자유 (매개변수화됨)

````markdown
## 보고서 생성

이 템플릿 사용:

```python
def generate_report(data, format="markdown", include_charts=True):
    # 처리 및 형식화
```
````

### 낮은 자유 (정확한 스크립트)

```markdown
## 데이터베이스 마이그레이션

정확히 실행:

```bash
python scripts/migrate.py --verify --backup
```

플래그를 수정하거나 추가하지 마세요.
```

---

## 빠른 규칙

### 하세요
✅ Claude의 지능을 가정하세요
✅ SKILL.md를 500줄 미만으로 유지하세요
✅ 점진적 공개를 사용하세요
✅ 대상 모델로 테스트하세요
✅ 구체적인 예시를 제공하세요
✅ 피드백 루프를 구축하세요
✅ 경로에 슬래시를 사용하세요

### 하지 마세요
❌ 명백한 개념을 설명하지 마세요
❌ 참조를 깊게 중첩하지 마세요
❌ 시간에 민감한 정보를 사용하지 마세요
❌ 용어를 섞지 마세요
❌ 설치된 도구를 가정하지 마세요
❌ Windows 경로를 사용하지 마세요
❌ 안내 없이 너무 많은 옵션을 제공하지 마세요

---

## 파일 구조 예시

### 간단한
```
skill/
└── SKILL.md
```

### 중간
```
skill/
├── SKILL.md
├── REFERENCE.md
└── EXAMPLES.md
```

### 복잡한
```
skill/
├── SKILL.md
├── reference/
│   ├── domain1.md
│   ├── domain2.md
│   └── domain3.md
└── scripts/
    ├── analyze.py
    ├── validate.py
    └── process.py
```

---

## 테스트 체크리스트

공유 전 빠른 확인:

- [ ] 설명에 무엇과 언제가 있음
- [ ] SKILL.md < 500줄
- [ ] 예시가 구체적임
- [ ] 대상 모델로 테스트됨
- [ ] 모든 경로에 슬래시 사용
- [ ] 용어가 일관됨

---

## 일반적인 수정

**문제:** 스킬이 트리거되지 않음
**수정:** 설명을 더 구체적으로 만들고 주요 용어 추가

**문제:** 일관되지 않은 동작
**수정:** 더 많은 구조 추가, 자유 수준 감소

**문제:** 토큰이 많이 듦
**수정:** 설명 제거, 참조 파일로 분할

**문제:** Claude가 혼란스러워함
**수정:** 구조 단순화, 명확한 예시 추가

---

## 개발 흐름

1. **Claude와 함께** 작업 수행
2. **반복적으로 제공하는 컨텍스트** 기록
3. **Claude에게** 패턴에서 스킬 생성 요청
4. **간결성** 검토
5. **실제 시나리오에서** 테스트
6. **관찰에 기반하여** 반복

---

## 도움받기

전체 문서 참조:
- [핵심 원칙](../principles/)
- [지침](../guidelines/)
- [완전한 체크리스트](checklist.md)
