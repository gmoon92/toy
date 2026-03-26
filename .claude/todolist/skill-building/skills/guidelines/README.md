# 스킬 개발 지침 (Skill Development Guidelines)

효과적인 스킬 문서 및 구현을 위한 실용적인 지침입니다.

## 문서

### [documentation-style.md](documentation-style.md)
**스킬 문서를 위한 완전한 스타일 가이드**

주제:
- 명확하고 구체적인 지시문 작성
- 스킬 문서 구조
- 실행 지시문 패턴
- 형식 제어를 위한 XML 태그
- 코드 예시 표준
- 점진적 공개
- 프론트매터 모범 사례

**읽어야 할 때:** 스킬 문서를 작성하거나 검토할 때

---

### [implementation-patterns.md](implementation-patterns.md)
**예시를 포함한 10가지 구체적인 구현 패턴**

패턴:
1. XML 구조화 출력
2. 단계 기반 참조 로딩
3. 강력한 지시문이 있는 스크립트 추출
4. 메타데이터 캐싱
5. 행동 우선 실행
6. 응답 전 조사
7. 병렬 도구 실행
8. 과잉 엔지니어링 방지
9. 명확한 성공 기준
10. 오류 복구 안내

**읽어야 할 때:** 특정 스킬 패턴을 구현할 때

---

## 빠른 참조

### 문서 스타일

```markdown
# 강력한 지시문 사용
EXECUTE_SCRIPT, 필수, 하지 마세요

# XML로 구조화
<phase_name>
콘텐츠
</phase_name>

# 점진적 공개
**로드:** references/guide.md
```

### 구현 패턴

**스크립트 추출:**
```bash
EXECUTE_SCRIPT: path/to/script.sh
```

**단계 기반 로딩:**
```markdown
## 단계 1: 분석
**로드:** references/phase1.md

## 단계 2: 구현
**로드:** references/phase2.md
```

**메타데이터 캐싱:**
```markdown
<analysis_results>
이 결과를 나중 단계에서 저장하세요.
</analysis_results>
```

---

## 작성 체크리스트

스킬 문서를 생성할 때:

- [ ] 프론트매터가 명확하고 구체적임
- [ ] 강력한 지시문 키워드를 일관되게 사용
- [ ] 예시가 입출력 형식과 함께 완전함
- [ ] 컨텍스트가 무엇(what)이 아닌 왜(why)를 설명
- [ ] 스크립트가 EXECUTE_SCRIPT 패턴 사용
- [ ] XML 태그가 복잡한 출력을 구조화
- [ ] 참조가 단계별로 구성됨
- [ ] 범위가 ✅/❌로 명확히 정의됨
- [ ] 능동태, 현재시제 전체에 사용
- [ ] 모호하거나 불분명한 언어 없음

---

## 함께 보기

- [../principles/](../principles/) - 핵심 원칙 (간결성, 적절한 자유)
- [../reference/](../reference/) - 빠른 가이드, 체크리스트
- [../../prompting/guidelines/](../../prompting/guidelines/) - 일반 프롬프팅 지침
