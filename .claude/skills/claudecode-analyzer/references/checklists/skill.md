# Skill 검증 체크리스트

## Skill 특화 프론트매터 검증

- [ ] `user-invocable: true` 시 `argument-hint` 필수
- [ ] `context: fork` 시 `agent` 필수
- [ ] `agent`: Explore, Plan, general-purpose만 사용
- [ ] `disable-model-invocation`: boolean
- [ ] `allowed-tools`: 문자열 배열
- [ ] `hooks`: 유효한 객체 형식
- [ ] `effort`: low, medium, high, max 중 하나

## Skill 특화 본문 구조 검증

### 필수 섹션
- [ ] 이름 및 설명
- [ ] Workflow
- [ ] Tool Use Examples

### 금지 섹션
- [ ] 본문에 "사용 예시", "트리거", "예시" 섹션 없음
- [ ] 사용 예시는 프론트매터 `description`에만

### 추가 검증
- [ ] 본문 500줄 이하
- [ ] Windows 경로(`\`) 없음
- [ ] MCP 도구 이름: `ServerName:tool_name`

## 토큰 효율성 검증 (INFO)

- [ ] SKILL.md가 100줄 이상인 경우, `When to Load References` 섹션 존재 (진행적 로딩 패턴)
- [ ] 반복적인 Bash/Python/JS 코드 블록이 3개 이상이면 `scripts/` 폴더로 분리 권장
- [ ] 스크립트 사용 시 `EXECUTE_SCRIPT:` 지시어 사용 여부
- [ ] 참조 문서는 1단계 깊이로 제한 (중첩 참조 없음)

## 강한 지시어 품질 검증 (INFO)

핵심 실행 지시에 약한 표현 사용 여부 확인 (개선 제안):

| 약한 표현 (경고) | 강한 표현 (권장) |
|:----------------|:----------------|
| `you should use` | `ALWAYS use` |
| `consider using` | `MANDATORY:` |
| `try to avoid` | `DO NOT` / `NEVER` |
| `it's better to` | `CRITICAL:` |
| `please don't` | `DO NOT` |

- [ ] 필수 동작에 `MANDATORY:` / `ALWAYS` 사용
- [ ] 금지 사항에 `DO NOT` / `NEVER` 사용
- [ ] 지시어 남용 없음 (선택적 제안에 강한 지시어 사용 금지)

## 문서 이름 품질 검증 (INFO)

- [ ] `name`: gerund 형태 권장 (`processing-pdfs`, `analyzing-data`)
- [ ] `name`: 구체적이고 범용적이지 않음 (`helper`, `utils` 금지)
- [ ] `description`: 3인칭 동사 시작 ("Processes files" — "Can help you process" 금지)
- [ ] `description`: 사용 예시가 구체적 (foo, bar 등 추상적 예시 금지)
- [ ] 하나의 개념에 하나의 용어 사용 (문서 전체에서 일관성)
- [ ] 시간 의존적 정보 없음 (날짜, "현재", "최신" 등 금지)

## 스크립트 품질 검증 (scripts/ 디렉토리 존재 시)

- [ ] 각 스크립트에 입출력 형식 문서화
- [ ] 종료 코드(exit code) 문서화
- [ ] 사용 예시 포함
- [ ] `scripts/README.md` 존재 (3개 이상 스크립트인 경우)

## 지시 자유도 검증 (INFO)

**참조**: [자유도 수준](../../skill-freedom-levels.md)

- [ ] (WARNING) 데이터 변경/삭제/시스템 작업에 강한 지시어(`MANDATORY`, `DO NOT`) 없이 약한 표현 사용
- [ ] (WARNING) 순서 준수 필수 작업에 "consider", "you might want" 등 약한 표현 사용
- [ ] (INFO) 창의적/분석 작업에 과도한 저자유도 스크립트 의존

## 배포 전 최종 검증 (INFO)

- [ ] 모든 파일 링크 유효성 확인
- [ ] 스크립트가 실제로 실행 가능한 상태
- [ ] TODO 주석 없음
- [ ] placeholder 콘텐츠 없음 (`foo`, `bar`, `example.com` 등)
- [ ] 참조 파일이 100줄 이상이면 목차(TOC) 포함
- [ ] 링크가 상대 경로 사용 (절대 경로 금지)

---

**참조**:
- [공통 프론트매터 검증](../common/frontmatter.md) - `name`, `description` 등
- [공통 구조 검증](../common/structure.md) - 헤딩 레벨, 경로 등
- [공통 참조 검증](../common/cross-reference.md) - 순환 참조, 파일 존재 등
- [가이드라인](../../guidelines.md)
- [구현 패턴](../../implementation-patterns.md) - 10가지 스킬 구현 패턴
- [강한 지시어](../../strong-directives.md) - 지시어 키워드 참조
- [전환 패턴](../../transformation-patterns.md)
