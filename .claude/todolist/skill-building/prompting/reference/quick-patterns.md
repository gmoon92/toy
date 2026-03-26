# 빠른 패턴 참조 (Quick Pattern Reference)

스킬 개발을 위한 일반적인 패턴입니다.

## XML 출력 구조

```markdown
<section_name>
특정 형식을 가진 콘텐츠.
</section_name>

<another_section>
다른 규칙을 가진 다른 콘텐츠.
</another_section>
```

---

## 스크립트 실행 지시문

```markdown
**필수: 미리 빌드된 스크립트 실행 (인라인 금지)**

```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
```

**중요:**
- 항상 EXECUTE_SCRIPT 지시문을 통해 미리 빌드된 스크립트 사용
- 명령어를 재구성하지 마세요
- 인라인 명령어를 사용하지 마세요
```

---

## 단계 기반 로딩

```markdown
## 참조 로딩 시기

### 단계 1: [이름]
- **로드**: [path/to/doc.md](path)
- **목적**: 간략한 설명
- **언제**: 트리거 조건

### 단계 2: [이름]
- **로드**: [path/to/doc.md](path)
- **목적**: 다른 기능
- **언제**: 다른 트리거
```

---

## 기본적으로 실행

```markdown
<default_to_action>
제안하는 대신 변경사항을 구현하는 것이 기본입니다.
사용자 의도를 추론하고 가장 유용한 작업을 진행하세요.
누락된 세부사항을 발견하기 위해 도구를 사용하세요.
</default_to_action>
```

---

## 먼저 조사

```markdown
<investigate_before_answering>
읽지 않은 코드에 대해 추측하지 마세요.
주장하기 전에 파일을 반드시 읽으세요.
근거 있고 환각 없는 답변만 제공하세요.
</investigate_before_answering>
```

---

## 과잉 엔지니어링 방지

```markdown
<avoid_overengineering>
직접 요청된 변경사항만 수행하세요.
해결책을 단순하고 집중적으로 유지하세요.
요구사항을 넘어선 기능을 추가하지 마세요.
</avoid_overengineering>
```

---

## 병렬 도구

```markdown
<use_parallel_tool_calls>
독립적인 도구 호출을 병렬로 실행하세요.
종속 작업에는 병렬을 사용하지 마세요.
</use_parallel_tool_calls>
```

---

## 메타데이터 캐싱

```markdown
### 분석 단계
<metadata_write>
{
  "analysis_complete": true,
  "change_type": "feature",
  "scope": "auth"
}
</metadata_write>

### 생성 단계
<metadata_read>
읽기: change_type, scope
</metadata_read>
```

---

## 성공 기준

```markdown
## 성공 기준

작업 완료 시점:
1. **[기준 1]** - 구체적인 결과
2. **[기준 2]** - 또 다른 결과
3. **[기준 3]** - 검증 요구사항

검증:
- [ ] 체크리스트 항목 1
- [ ] 체크리스트 항목 2
```

---

## 오류 처리

```markdown
## 오류 처리

**오류:** [패턴]
**원인:** [이유]
**복구:**
1. 진단 단계
2. 수정 단계
3. 검증 단계
4. 재시도 단계
```

---

## 범위 정의

```markdown
## 범위

**하세요:**
- ✅ 구체적인 작업 1
- ✅ 구체적인 작업 2

**하지 마세요:**
- ❌ 범위 밖 작업 1
- ❌ 범위 밖 작업 2
```
