# Frontmatter Documents fetch

Claude Code 공식 문서를 기반으로 프론트매터 문서를 최신 정보로 업데이트합니다.

**Important**: 추론하지 말고, 반드시 공식 문서를 기반으로 사실만 작성합니다. 한글로 작성합니다.

- 대상: `${CLAUDE_PROJECT_DIR}/.claude/docs/claude-code-meta/frontmatter` 하위 모든 `.md` 문서
    - `agent.md`
    - `claude.md`
    - `rule.md`
    - `skill.md`
- [고정 템플릿](#Template)을 사용합니다.

## Workflow

1. 문서 유형에 따라 아래 공식 문서를 반드시 참고합니다.

**Important**: 공식 문서 링크 중 하나라도 접근할 수 없는 경우, 사용자에게 알린 후 작업을 중단합니다.

- 공통: [Claude Code Release Notes](https://api.github.com/repos/anthropics/claude-code/releases/latest)

| 문서                     | 공식 문서 링크                                                                                                                                                                                                                                                                                                                |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `agent.md`             | [Sub Agent: Supported frontmatter fields](https://code.claude.com/docs/en/sub-agents#supported-frontmatter-fields)                                                                                                                                                                                                      |
| `skill.md`             | [Agent Skills: Frontmatter reference](https://code.claude.com/docs/en/skills#frontmatter-reference)                                                                                                                                                                                                                     |
| `claude.md`, `rule.md` | [CLAUDE.md files](https://code.claude.com/docs/en/memory#claude-md-files)<br/>[How Claude remembers your project](https://code.claude.com/docs/en/memory#how-claudemd-files-load), <br/>[Organize rules with `.claude/rules/`](https://code.claude.com/docs/en/memory#organize-rules-with-claude/rules/)                |

2. [고정 템플릿](#Template)을 사용합니다.
3. `claude-code-version`에는 Claude Code Release Notes의 최신(`Latest`) 버전을 명시합니다.
4. `<version-updated-date>`에는 해당 버전의 Release Notes에 명시된 날짜를 기재합니다.
5. `last-updated`는 프론트매터 정의에 변경이 발생한 경우에 갱신합니다.
    - 필드 추가 / 삭제
    - 필드 스펙 변경 (타입, 필수 여부, 기본값, 허용값/범위)
    - 설명 또는 제약 조건 변경
    - deprecated 상태 변경
6. `Custom Extensions` 섹션은 사용자가 직접 작성한 내용입니다. 작업 과정에서 절대 수정하지 않습니다.
7. `Fields`
   - 추론하지 말고, 반드시 공식 문서를 기반으로 사실만 작성합니다.
   - 필드가 없는 경우, 테이블을 작성하지 않고 다음 문장을 삽입합니다.  
     `"<문서 유형> 문서는 **프론트매터가 없는** 일반 마크다운 문서입니다."`
   - 필드 정보는 다음 기준에 따라 작성합니다.
       - `필드`: 실제 프론트매터 키 이름을 그대로 작성합니다.
       - `필수`: `Required` 또는 `Optional`만 사용합니다.
       - `타입`: 공식 문서에 명시된 타입을 그대로 사용합니다. (예: string, boolean, array<string>)
       - `기본값`: 명시된 경우에만 작성하며, 없을 경우 `none`으로 표기합니다.
       - `허용값/범위`: enum, 범위, 패턴 등이 명시된 경우에만 작성합니다. 없으면 `-`로 표기합니다.
       - `설명`: 필드의 역할을 한 줄로 명확하게 한글로 작성합니다.
       - `비고/제약`: 특정 문서 유형 전용, 다른 필드와의 의존성 등 추가 조건이 있을 경우만 작성합니다.
   - 모든 placeholder는 반드시 아래 포맷을 사용합니다.
       - `<field>`, `<type>`, `<default>`, `<allowed_values>`, `<description>`, `<notes>`
   - 값이 없는 경우 아래 기준으로 표기합니다.
       - 기본값 없음: `none`
       - 허용값 없음: `-`
       - 비고 없음: 공란
   - `version` 항목은 추론으로 처리하지 않습니다.
     - 포맷: `<version> (<date>)`
   - `deprecated` 항목은 추론으로 처리하지 않습니다.
       - 공식 문서에서 `deprecated` 또는 제거된 경우에만 표시합니다.
       - 포맷: `deprecated@<version> (<date>)`
       - 버전 정보가 없는 경우: `deprecated (<date>)`
       - 날짜 정보가 없는 경우: `deprecated (last-updated: YYYY-MM-DD)`
           - `YYYY-MM-DD`는 참조한 공식 문서의 마지막 업데이트 일자를 사용합니다.
8. Release Notes 검증
    - 공식 문서에 명시된 내용을 우선 기준으로 반영합니다.
    - [Claude Code Release Notes](https://api.github.com/repos/anthropics/claude-code/releases/latest) `Latest` 버전 내용을 반드시 확인합니다.
    - Release Notes는 참고용으로 활용합니다.
    - Release Notes에 없는 변경이라도, 공식 문서에 명시된 경우 반영합니다.
    - 현재 문서와 이전 버전을 비교하여 다음을 검토합니다.
        - 프론트매터 필드 추가 여부
        - 프론트매터 필드 삭제 여부
        - 필드 스펙 변경 (타입, 필수 여부 등)
    - 변경 사항이 확인되면 다음을 수행합니다.
        - 신규 필드는 최초 등장 시점에만 테이블에 추가하고, `version` 항목에 `<version>`을 명시합니다.
        - 이후 버전에서는 중복 기록하지 않습니다.
        - 제거된 필드는 deprecated 처리합니다.
        - 기존 필드의 스펙 변경은 필요 시 `비고`에 변경 내용을 간단히 기록합니다.
9. `Note` 섹션은 공식 문서에 명시되지 않은 추가 설명이 필요한 경우에만 작성합니다.

## Template

````markdown
# <문서 유형> Frontmatter

## Metadata

- claude-code-version: vX.Y.Z (<version-updated-date>)
- last-updated: YYYY-MM-DD

## Custom Extensions

## Fields

| 필드              |          필수           |    타입    |     기본값     | 허용값/범위             | 설명              | version     | deprecated                      | 비고/제약     |
|:----------------|:---------------------:|:--------:|:-----------:|:-------------------|:----------------|-------------|:--------------------------------|:----------|
| `<field>`       |  Required / Optional  | `<type>` | `<default>` | `<allowed_values>` | `<description>` | <version> (<date>) |                                 | `<notes>` |
| ~~`<field>`~~   |  Required / Optional  | `<type>` | `<default>` | `<allowed_values>` | `<description>` | <version> (<date>) | deprecated@<version> (<date>)   | `<notes>` |

## Note

## Reference

- <공식 문서 링크 1>
- <공식 문서 링크 2>

````

