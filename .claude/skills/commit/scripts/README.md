# Commit Skill Scripts

결정론적이고 컨텍스트를 소비하지 않는 스크립트 모음입니다.

## 개요

Agent Skills 아키텍처의 **레벨 3: 리소스 및 코드**를 활용:
- ✅ **컨텍스트 소비 없음** - bash를 통해 실행되며 출력만 컨텍스트에 포함
- ✅ **결정론적** - 동일한 입력에 대해 항상 동일한 출력
- ✅ **빠른 실행** - LLM 추론 없이 직접 실행

---

## validate_message.py

커밋 메시지 형식을 검증하고 금지된 패턴(Co-Authored-By)을 감지합니다.

### 기본 사용법

```bash
# 파일에서 검증
python3 scripts/validate_message.py --message /tmp/commit-msg.txt --json

# stdin에서 검증
echo "feat(auth): add JWT" | python3 scripts/validate_message.py --stdin --json
```

### Exit codes

- `0`: Valid message
- `1`: Invalid message (errors found)
- `2`: File not found or read error

### 상세 사용법

스크립트 실행 시 `--help` 옵션으로 확인:
```bash
python3 scripts/validate_message.py --help
```

---

## 참고 자료

- [Agent Skills Documentation](https://platform.claude.com/docs/ko/agents-and-tools/agent-skills/overview)
- [Commit Skill 설계 문서](../SKILL.md)
- [실행 가이드](../references/process/step5-execute.md)
