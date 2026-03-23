---
name: reader
description: 독자 관점 검증 체크리스트 - 선행지식, 이핏도, 실용성, 예시 관련성
---

# 독자 관점 검증 체크리스트 (Reader Mode)

## 1. 대상 독자 분석 (Audience Analysis)

- [ ] **대상 명시**: 문서의 대상 독자가 명확히 정의되어 있는가?
- [ ] **수준 적절성**: 내용의 난이도가 대상 독자에 적합한가?
- [ ] **선행지식**: 필요한 선행지식이 명시되어 있는가?
- [ ] **확대 가능성**: 더 넓은 독자층을 위한 확대 방안이 있는가?

## 2. 이핏도 검증 (Comprehension)

### Easy to Understand
- [ ] **직관적 설명**: 핵심 개념이 직관적으로 설명되는가?
- [ ] **단계별 접근**: 복잡한 과정이 단계별로 나뉘어 있는가?
- [ ] **시각적 보조**: 다이어그램, 표, 이미지가 이해를 돕는가?

### Moderate Effort Required
- [ ] **충분한 맥락**: 새로운 개념에 충분한 배경 설명이 있는가?
- [ ] **용어 정의**: 전문 용어가 적절한 시점에 정의되는가?
- [ ] **링크 제공**: 추가 학습을 위한 참조 링크가 있는가?

### Potentially Confusing
- [ ] **갑작스런 전환**: 주제 전환이 너무 급작스럽지 않은가?
- [ ] **누락된 단계**: 중요한 중간 단계가 생략되어 있지 않은가?
- [ ] **암묵적 가정**: 독자가 알 것이라 암묵적으로 가정하는 것은 없는가?

## 3. 실용성 평가 (Practical Value)

- [ ] **즉시 적용**: 읽은 내용을 즉시 적용할 수 있는가?
- [ ] **실행 가능한 조치**: 구체적인 실행 단계가 제공되는가?
- [ ] **성과 측정**: 성공을 평가할 수 있는 기준이 있는가?
- [ ] **시간 대비 가치**: 투자한 시간에 비해 얻는 가치가 충분한가?

## 4. 예시 관련성 (Examples)

- [ ] **현실적 예시**: 예시가 현실적이고 관련성이 있는가?
- [ ] **다양한 사례**: 다양한 사용 사례를 커버하는가?
- [ ] **복잡도 단계**: 간단한 예시부터 복잡한 예시까지 단계별로 제공되는가?
- [ ] **독자 연결**: 예시가 독자의 실제 상황과 연결되는가?

## 5. 참여도 (Engagement)

- [ ] **훅 품질**: 서례이 독자의 관심을 끌 수 있는가?
- [ ] **흥미 유지**: 문서 전체에 걸쳐 흥미를 유지하는가?
- [ ] **상호작용**: 독자가 직접 시도핸 것을 권장하는가?
- [ ] **행동 촉구**: 명확한 다음 단계/행동을 제시하는가?

## 6. 접근성 (Accessibility)

- [ ] **소제목 활용**: 스캐닝을 위한 명확한 소제목이 있는가?
- [ ] **요약 제공**: 각 섹션의 핵심을 요약하는가?
- [ ] **길이 적절성**: 문서 길이가 목적에 적합한가?
- [ ] **언어 장벽**: 비영어권 독자를 위한 고려가 있는가?

---

## 심각도 분류

| 등급 | 기준 | 처리 |
|-----|------|------|
| **CRITICAL** | 대상 독자가 내용을 이해할 수 없음 | 반드시 수정 |
| **WARNING** | 이해에 상당한 노력 필요 | 수정 권장 |
| **INFO** | 개선 가능 사항 | 선택적 반영 |

---

## 출력 형식

```markdown
## 독자 관점 검증 (reader)

### Audience Analysis
- Target fit: [Good/Partial/Misaligned]
- Prerequisite knowledge required: [필요한 선행지식 목록]
- Potential audience expansion: [확대 제안]

### Comprehension Checkpoints

#### Easy to Understand
- [섹션]: [왜 이해하기 쉬운지]

#### Moderate Effort Required
- [섹션]: [어려운 점과 극복 방법]

#### Potentially Confusing
- [섹션]: [문제점]
  Suggestion: [개선 제안]

### Missing Context
- [주제]: [누락된 맥락] - [독자가 왜 필요로 하는지]

### Practical Value Assessment
- Immediate applicability: [High/Medium/Low]
- Actionable takeaways: [핵심 포인트 목록]
- Implementation guidance: [Adequate/Needs Work]

### Suggested Beginner Additions
- [위치]: [추가할 내용]
- [위치]: [도움이 될 예시]

### Engagement Feedback
- Hook quality: [Strong/Weak]
- Section to trim: [제거 제안 섹션]
- Most valuable section: [가장 가치 있는 섹션과 이유]
```
