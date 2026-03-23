---
name: structure
description: 구조 분석 체크리스트 - 섹션 중복, 계층 일관성, 흐름, 균형, 네비게이션
---

# 구조 분석 체크리스트 (Structure Mode)

## 1. 정보 구조 (Information Architecture)

- [ ] **논리적 그룹화**: 관련 내용이 적절히 그룹화되어 있는가?
- [ ] **계층 깊이**: 헤딩 레벨이 적절한 깊이(3-4단계)를 유지하는가?
- [ ] **일관된 패턴**: 유사한 내용이 일관된 구조로 제시되는가?
- [ ] **중요도 순서**: 중요한 내용이 먼저 제시되는가?

## 2. 중복 검사 (Redundancy Check)

- [ ] **섹션 간 중복**: 같은 내용이 여러 섹션에 반복되지 않는가?
- [ ] **개념 중복**: 동일 개념이 다른 표현으로 중복 설명되지 않는가?
- [ ] **예시 중복**: 동일한 예시가 불필요하게 반복되지 않는가?
- [ ] **참조 중복**: 동일한 참조가 여러 번 언급되지 않는가?

## 3. 흐름 분석 (Flow Analysis)

- [ ] **자연스러운 전환**: 섹션 간 전환이 매끄러운가?
- [ ] **인과 관계**: 원인-결과 관계가 논리적으로 연결되는가?
- [ ] **시간 순서**: 시간적 흐름이 있는 경우 순서가 올바른가?
- [ ] **난이도 순서**: 개념이 난이도 순으로 제시되는가?
- [ ] **독자 여정**: 독자가 목표 지점까지 명확한 경로를 따라가는가?

## 4. 균형 평가 (Balance Assessment)

- [ ] **섹션 길이**: 각 섹션의 길이가 적절히 균형을 이루는가?
- [ ] **중요도 비율**: 중요한 주제에 충분한 분량이 할당되었는가?
- [ ] **깊이 일관성**: 섹션 간 설명의 깊이가 일관되는가?
- [ ] **예시 분배**: 예시가 전체 문서에 고르게 분포되어 있는가?

## 5. 네비게이션 (Navigation)

- [ ] **목차 존재**: 긴 문서에 목차(Table of Contents)가 있는가?
- [ ] **앵커 링크**: 헤딩에 앵커 링크가 제공되는가?
- [ ] **상위/하위 이동**: 계층 구조 내 이동이 용이한가?
- [ ] **관련 링크**: 관련 섹션 간 연결(link)이 있는가?
- [ ] **검색 가능성**: 헤딩이 검색하기 쉬운 키워드를 포함하는가?

## 6. 헤딩 품질 (Heading Quality)

- [ ] **명확성**: 각 헤딩이 섹션 내용을 명확히 설명하는가?
- [ ] **일관된 형식**: 헤딩 형식(명사형, 동사형 등)이 일관되는가?
- [ ] **계층 일관성**: 헤딩 레벨이 논리적 계층을 따르는가?
- [ ] **길이 적절성**: 헤딩이 너무 길거나 짧지 않은가?
- [ ] **SEO 고려**: 헤딩이 검색 엔진에 적합한 키워드를 포함하는가?

## 7. 시각화 검토 (Visualization)

- [ ] **적절한 표 사용**: 표는 비교/정보 전달에 효과적으로 사용되는가?
- [ ] **과도한 시각화**: 표/다이어그램이 너무 많아 서술을 방해하지 않는가?
- [ ] **서술 vs 표**: 복잡한 내용은 서술이 더 적절한가?
- [ ] **시각적 균형**: 텍스트와 시각적 요소의 균형이 적절한가?
- [ ] **간결한 표**: 표의 항목이 너무 많지 않은가? (5-7개 권장)

## 8. 개선 제안 (Improvement Suggestions)

- [ ] **구조 재구성**: 더 나은 정보 구조를 제안할 수 있는가?
- [ ] **섹션 통합**: 통합할 수 있는 중복 섹션이 있는가?
- [ ] **섹션 분리**: 분리하면 더 명확해질 섹션이 있는가?
- [ ] **순서 변경**: 순서를 바꾸면 흐름이 개선되는가?

---

## 심각도 분류

| 등급 | 기준 | 처리 |
|-----|------|------|
| **CRITICAL** | 심각한 구조 문제로 내용 파악 불가 | 반드시 수정 |
| **WARNING** | 중복이나 흐름 문제로 가독성 저하 | 수정 권장 |
| **INFO** | 개선 가능 사항 | 선택적 반영 |

---

## 출력 형식

```markdown
## 구조 분석 (structure)

### Structure Assessment
- Overall organization: [Excellent/Good/Needs Work]
- Section hierarchy: [Clear/Confusing/Mixed]
- Redundancy issues: [중복 내용 영역 목록]

### Redundancy Analysis
- [위치1]와 [위치2]의 내용 중복: [통합 제안]

### Flow Analysis
- Section transitions: [Smooth/Disjointed/Needs Improvement]
- Logical progression: [Strong/Weak]
- Reader journey: [Clear/Unclear]

### Balance Assessment
- Section length distribution: [Balanced/Skewed]
- Over-emphasized: [과다 분량 섹션]
- Under-emphasized: [부족한 분량 섹션]

### Navigation Check
- TOC present: [Yes/No]
- Anchor links: [Complete/Partial/Missing]
- Cross-references: [Adequate/Needs Work]

### Improvement Recommendations
1. [위치]: [구체적인 개선 제안]
2. [위치]: [구체적인 개선 제안]

### Proposed Structure (if different)
```
[개선된 구조 개요]
- 1. [새로운 섹션 구조]
  - 1.1 [하위 섹션]
  - 1.2 [하위 섹션]
- 2. [새로운 섹션 구조]
```
```
