# 스킬 템플릿 (Skill Template)

새 스킬을 생성할 때 이 템플릿을 사용하세요.

---

## SKILL.md 템플릿

```markdown
---
name: skill-name
description: 이 스킬이 하는 것에 대한 간략한 설명. 200자 미만으로 유지.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, AskUserQuestion, Write, Edit
---

# YAML 프론트매터 검증 규칙:
#
# name (필수):
#   - 최대 64자
#   - 소문자, 숫자, 하이픈만 포함
#   - XML 태그 포함 불가
#   - 예약어 불가: "anthropic", "claude"
#   - 동명사 형태(동사-ing) 사용: processing-pdfs, analyzing-data
#
# description (필수):
#   - 비어 있으면 안 됨
#   - 최대 1024자
#   - XML 태그 포함 불가
#   - Claude가 인식해야 할 주요 용어 포함
#   - 이 스킬을 언제 사용하는지 설명

## 개요

스킬의 목적과 역량에 대한 상세 설명.

**핵심 기능:**
- 기능 1: 설명
- 기능 2: 설명
- 기능 3: 설명

**범위:**
- ✅ 이 스킬이 하는 것
- ✅ 이 스킬이 처리하는 것
- ❌ 절대: 이 스킬이 피하는 것
- ❌ 절대: 범위 밖 작업

## 참조 로딩 시기

스킬을 단계별로 실행. 최대 토큰 효율성을 위해 단계별로 필요한 참조만 로드.

### 단계 1: [단계 이름]
- **로드**: [reference/path.md](references/reference/path.md)
- **목적**: 이 단계가 하는 것
- **언제**: 이 단계를 실행할 때

### 단계 2: [단계 이름] (조걶)
- **로드**: [reference/path.md](references/reference/path.md)
- **목적**: 이 단계가 하는 것
- **언제**: [조건]일 때만

[... 필요한 만큼 더 많은 단계 ...]

### 지원 리소스 (필요에 따라 로드)
- **리소스 1**: [path.md](references/path.md) - 사용 시기
- **리소스 2**: [path.md](references/path.md) - 사용 시기

### 스크립트 (해당되는 경우)

**중요:** 스크립트는 미리 빌드되고 결정적인 실행 파일. 에이전트는 반드시 `EXECUTE_SCRIPT:` 지시문을 사용해야 함.

**단계 X - [작업]:**
- `EXECUTE_SCRIPT: scripts/category/script_name.sh` - 하는 것

**문서:** 자세한 사용법은 [scripts/README.md](scripts/README.md) 참조

## 핵심 원칙

**1. 원칙 이름**
- 원칙 설명
- 왜 중요한지
- 어떻게 적용하는지

**2. 원칙 이름**
- 원칙 설명
- 왜 중요한지
- 어떻게 적용하는지

[... 필요한 만큼 더 많은 원칙 ...]

## 빠른 참조

**형식/패턴**: 설명

**핵심 개념:**
- 개념 1: 설명
- 개념 2: 설명

## 실행 흐름

```
1. 단계 1     → [참조](references/step1.md)
2. 단계 2     → [참조](references/step2.md) (필요시)
3. 단계 3     → [참조](references/step3.md)
...
```

각 단계는 필요한 참조만 로드. 자세한 내용은 위의 "참조 로딩 시기" 참조.

## 사용법

**스킬 호출:**
```
/skill-name
```

**자연어:**
```
예시 명령어 1
예시 명령어 2
예시 명령어 3
```
```

---

## 디렉토리 구조 템플릿

```
skill-name/
├── SKILL.md                 # 주요 스킬 문서
├── README.md               # 선택사항: 추가 컨텍스트
├── references/             # 상세 참조 문서
│   ├── process/           # 단계별 프로세스 문서
│   │   ├── step1.md
│   │   ├── step2.md
│   │   └── step3.md
│   ├── guides/            # 하우투 가이드
│   │   └── guide-name.md
│   ├── validation/        # 검증 규칙
│   │   └── rules.md
│   └── support/           # 지원 문서
│       ├── examples.md
│       ├── troubleshooting.md
│       └── metadata.md
├── scripts/               # 실행 가능한 스크립트(선택사항)
│   ├── README.md
│   ├── analysis/
│   ├── algorithms/
│   ├── generation/
│   ├── validation/
│   ├── execution/
│   └── utils/
└── templates/             # UI 템플릿(컨텍스트에 로드되지 않음)
    └── template-name.md
```

---

## 스크립트 템플릿 (Bash)

```bash
#!/usr/bin/env bash
# script_name.sh
# 목적: 이 스크립트가 하는 것에 대한 한 줄 설명
# 사용법: ./script_name.sh < input.json > output.json

set -e  # 오류 시 종료

# stdin에서 입력 파싱
if [ -t 0 ]; then
    echo "오류: 입력은 stdin을 통해 제공되어야 함" >&2
    echo "사용법: echo '{\"key\":\"value\"}' | $0" >&2
    exit 1
fi

INPUT_DATA=$(cat)

# 입력 처리
# ... 구현 ...

# JSON으로 결과 출력
echo "{"
echo "  \"status\": \"success\","
echo "  \"result\": \"...\""
echo "}"
```

---

## 스크립트 템플릿 (JavaScript/Node.js)

```javascript
#!/usr/bin/env node
/**
 * script_name.js
 * 목적: 이 스크립트가 하는 것에 대한 한 줄 설명
 * 사용법: echo '{"key":"value"}' | node script_name.js
 * 입력: {"key": "value", ...}
 * 출력: {"status": "success", "result": {...}}
 */

function processData(input) {
  // 구현
  return {
    status: 'success',
    result: {}
  };
}

// 주요 실행
if (require.main === module) {
  let inputData = '';

  process.stdin.on('data', chunk => {
    inputData += chunk;
  });

  process.stdin.on('end', () => {
    try {
      const input = JSON.parse(inputData);
      const output = processData(input);
      console.log(JSON.stringify(output, null, 2));
    } catch (error) {
      console.error(JSON.stringify({
        status: 'failed',
        error: error.message
      }));
      process.exit(1);
    }
  });
}

module.exports = { processData };
```

---

## 스크립트 템플릿 (Python)

```python
#!/usr/bin/env python3
"""
script_name.py
목적: 이 스크립트가 하는 것에 대한 한 줄 설명
사용법: echo '{"key":"value"}' | python3 script_name.py
"""

import sys
import json

def process_data(input_data):
    """입력을 처리하고 결과를 반환."""
    # 구현
    return {
        'status': 'success',
        'result': {}
    }

if __name__ == '__main__':
    try:
        # stdin에서 읽기
        input_data = json.load(sys.stdin)

        # 처리
        output = process_data(input_data)

        # JSON으로 출력
        print(json.dumps(output, indent=2))

    except json.JSONDecodeError as e:
        print(json.dumps({
            'status': 'failed',
            'error': f'잘못된 JSON 입력: {e}'
        }), file=sys.stderr)
        sys.exit(1)

    except Exception as e:
        print(json.dumps({
            'status': 'failed',
            'error': str(e)
        }), file=sys.stderr)
        sys.exit(1)
```

---

## 참조 문서 템플릿

```markdown
# 문서 제목

이 문서가 다루는 것에 대한 간략한 개요.

---

## 목적

이 문서의 목적과 언제 사용하는지 설명.

## 내용

### 섹션 1

상세 내용...

### 섹션 2

상세 내용...

## 예시

개념을 보여주는 구체적인 예시.

## 관련 문서

- [관련 문서 1](path/to/doc1.md)
- [관련 문서 2](path/to/doc2.md)
```

---

## 사용법

1. 이 템플릿을 새 스킬 디렉토리에 복사
2. 플레이스홀더를 실제 콘텐츠로 교체
3. 해당하지 않는 섹션 제거
4. 필요에 따라 추가 섹션 추가
5. 명명 규칙과 디렉토리 구조 따르기
