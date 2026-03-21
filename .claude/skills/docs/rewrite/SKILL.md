---
name: rewrite
description: 문서 품질을 작성-검증 피드백 루프로 개선합니다. 작성자 에이전트가 초안을 작성하고 검증자 에이전트가 피드백을 제공하는 과정을 반복하여 문서를 정제합니다.
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash, AskUserQuestion, Agent
---

# 문서 재작성 (Doc Rewrite)

문서 품질을 작성-검증 피드백 루프로 개선합니다.

## 개요

1. [@doc-writer](../../../agents/docs/writer.md) 에이전트가 초안을 작성합니다.
2. [@doc-reviewer](../../../agents/docs/reviewer.md) 에이전트가 초안을 검토하고 피드백을 제공합니다.
3. 모든 항목이 충족되면 완료, 미충족 시 작성자에게 수정을 지시합니다.
4. 최대 3라운드까지 피드백 루프를 반복합니다.

## 실행

1. 임시 디렉토리 `${CLAUDE_TMP_DIR}`를 생성합니다.
2. **백업 생성**: 작업 대상 파일이 있다면 `.backup` 확장자로 백업을 생성합니다.
3. 작성자 에이전트를 생성하여 초안 작성을 지시합니다.
4. 작성 완료 후 검증자 에이전트를 생성하여 검증을 지시합니다.
5. 피드백 체크리스트를 확인하여 완료 여부를 판정합니다.
6. 미완료 항목이 있으면 작성자 에이전트에게 수정을 지시하고 3단계로 돌아갑니다.
7. 3라운드 내에 완료되지 않으면 사용자에게 선택을 요청합니다.
8. 모든 항목이 완료되면 최종본을 원본에 반영하고 임시 디렉토리를 정리합니다.

## 연결 에이전트

- 작성자: [@doc-writer](../../../agents/docs/writer.md)
- 검증자: [@doc-reviewer](../../../agents/docs/reviewer.md)

## 사용 예시

```
/skill:docs/rewrite ai/docs/claude/tips/claude-tips.md
```

또는

```
이 문서 개선해줘
문서 품질 높여줘
```
