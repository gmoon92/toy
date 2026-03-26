# 스크립트 추출 가이드 (Script Extraction Guide)

문서에서 인라인 코드를 실행 가능한 스크립트 파일로 추출하기 위한 포괄적인 가이드입니다.

---

## 왜 스크립트를 추출하는가?

### 인라인 코드의 문제

**1. 토큰 비효율성**
- 모든 인라인 코드 블록이 컨텍스트 토큰을 소비
- Claude는 실행 시마다 코드를 읽고 파싱해야 함
- 일반적인 비용: 인라인 스크립트당 500-2000 토큰

**2. 비결정적 실행**
- Claude가 매번 코드를 다르게 해석할 수 있음
- 코드 재구성이 오류를 도입할 수 있음
- 일관된 동작 보장 없음

**3. 유지보수 부담**
- 알고리즘 변경이 문서 업데이트를 요구
- 여러 파일에 코드 중복
- 독립적으로 테스트하기 어려움

**4. 관심사 분리**
- 문서와 실행 가능한 코드가 섞임
- 탐색과 이해가 더 어려움
- "무엇(what)"과 "어떻게(how)"의 경계가 모호

### 스크립트 추출의 이점

**토큰 효율성:**
- 스크립트는 컨텍스트 토큰 없이 실행됨
- 출력만 로드됨(스크립트 코드 자체는 아님)
- 일반적인 절약: 스킬당 1000-3000 토큰

**결정적 실행:**
- 같은 입력은 항상 같은 출력 생성
- 사전 테스트된, 검증된 코드
- 일관되고 예측 가능한 동작

**쉬운 유지보수:**
- 문서를 건드리지 않고 스크립트 업데이트
- 스크립트를 독립적으로 테스트
- 명확한 관심사 분리

**재사용성:**
- 스크립트는 다른 도구에서 사용 가능
- 격리하여 테스트 가능
- 버전 관리 및 변경 추적이 쉬움

---

## 스크립트를 추출해야 할 때

### 추출하기 좋은 후보

**✅ 복잡한 알고리즘:**
- 파일 분석 및 패턴 감지
- 범위/유형 감지 로직
- 콘텐츠 생성 알고리즘

**✅ 다단계 작업:**
- 데이터 수집 및 변환
- 여러 검사가 있는 검증
- 순차적 처리 파이프라인

**✅ 오류가 발생하기 쉬운 작업:**
- Git 작업(커밋, diff, merge)
- 파일 시스템 작업
- API 호출 및 파싱

**✅ 자주 반복되는 코드:**
- 여러 번 사용되는 같은 패턴
- 단계별 공통 작업
- 유틸리티 함수

### 추출하기 나쁜 후보

**❌ 간단한 한 줄짜리:**
```bash
ls -la
git status
echo "완료"
```

**❌ 매우 변동성이 높은 작업:**
- 컨텍스트에 따라 변경되는 코드
- 인간의 판단이 필요한 작업
- 탐색적 명령어

**❌ 데모 예시:**
- 교육 목적으로 표시된 코드
- API 사용 예시
- 패턴 설명

---

## 스크립트 추출 프로세스

### 단계 1: 인라인 코드 식별

**코드 블록 검색:**

```bash
# 모든 bash 스크립트 찾기
find skill-name -name "*.md" -exec grep -l '```bash' {} \;

# JavaScript 코드 찾기
find skill-name -name "*.md" -exec grep -l '```javascript' {} \;

# Python 코드 찾기
find skill-name -name "*.md" -exec grep -l '```python' {} \;
```

**발견사항 카탈로그:**
```markdown
## 인라인 코드 목록

### step1-analysis.md
- bash (45-67줄): Git 변경사항 수집 및 메타데이터 생성
- bash (89-95줄): Git 상태 확인

### step5-execute.md
- bash (120-145줄): 커밋 검증 및 실행

### algorithms.md
- javascript (30-80줄): 파일에서 범위 감지
- javascript (90-140줄): 변경사항에서 유형 감지
```

### 단계 2: 스크립트 아키텍처 설계

**책임별로 구성:**

```
scripts/
├── README.md              # 포괄적인 문서
├── analysis/             # 데이터 수집 및 분석
│   ├── collect_changes.sh
│   └── create_metadata.sh
├── algorithms/           # 감지 및 의사결정 로직
│   ├── detect_scope.js
│   └── detect_type.js
├── generation/           # 콘텐츠 생성
│   └── generate_message.js
├── validation/           # 입출력 검증
│   └── validate_output.py
├── execution/            # 주요 작업
│   └── execute_commit.sh
└── utils/                # 도우미 함수
    └── cleanup.sh
```

**핵심 원칙:**
- **단일 책임**: 하나의 스크립트 = 하나의 명확한 목적
- **명확한 명명**: 동사_명사 패턴(예: `collect_changes.sh`)
- **논리적 그룹화**: 시간순이 아닌 카테고리별로 구성
- **구성 가능성**: 스크립트를 파이프로 연결 가능

### 단계 3: 스크립트 언어 선택

**Bash** - 시스템 작업:
- 파일 시스템 작업
- Git 명령어
- 프로세스 관리
- 텍스트 처리(jq로 JSON)

**JavaScript/Node.js** - 알고리즘:
- 복잡한 로직과 의사결정
- JSON 조작
- 문자열 처리
- 파싱 및 패턴 매칭

**Python** - 데이터 처리:
- 검증 및 확인
- 데이터 변환
- 복잡한 계산
- API 상호작용

**의사결정 기준:**
- 작동하는 가장 간단한 언어 사용
- 작업 유형에 언어 매칭
- 종속성 및 설정 고려
- 셸 작업에는 bash 선호

### 단계 4: 스크립트 작성

**스크립트 템플릿 구조:**

#### Bash 스크립트 템플릿

```bash
#!/usr/bin/env bash
# script_name.sh
# 목적: 이 스크립트가 하는 것에 대한 한 줄 설명
# 사용법: ./script_name.sh < input.json > output.json
# 입력: JSON 형식 설명
# 출력: JSON 형식 설명

set -e  # 오류 시 종료
set -u  # 정의되지 않은 변수 시 종료
set -o pipefail  # 파이프 실패 시 종료

# stdin에서 입력 파싱
if [ -t 0 ]; then
    echo "오류: 입력은 stdin을 통해 제공되어야 함" >&2
    echo "사용법: echo '{\"key\":\"value\"}' | $0" >&2
    exit 1
fi

INPUT_DATA=$(cat)

# JSON에서 필드 추출(jq가 있으면 사용)
if command -v jq &> /dev/null; then
    FIELD=$(echo "$INPUT_DATA" | jq -r '.field')
else
    # jq 없는 폴백 파싱
    FIELD=$(echo "$INPUT_DATA" | grep -o '"field":"[^"]*"' | cut -d'"' -f4)
fi

# 입력 처리
# ... 구현 ...

# JSON으로 결과 출력
echo "{"
echo "  \"status\": \"success\","
echo "  \"result\": \"...\""
echo "}"
```

#### JavaScript/Node.js 스크립트 템플릿

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
  // 입력 검증
  if (!input || typeof input !== 'object') {
    throw new Error('잘못된 입력: 객체 예상');
  }

  // 구현
  const result = {
    // ... 처리 로직 ...
  };

  return {
    status: 'success',
    result: result
  };
}

// 주요 실행
if (require.main === module) {
  let inputData = '';

  process.stdin.setEncoding('utf8');

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

#### Python 스크립트 템플릿

```python
#!/usr/bin/env python3
"""
script_name.py
목적: 이 스크립트가 하는 것에 대한 한 줄 설명
사용법: echo '{"key":"value"}' | python3 script_name.py
"""

import sys
import json
from typing import Dict, Any

def process_data(input_data: Dict[str, Any]) -> Dict[str, Any]:
    """입력을 처리하고 결과를 반환."""
    # 입력 검증
    if not isinstance(input_data, dict):
        raise ValueError('잘못된 입력: 딕셔너리 예상')

    # 구현
    result = {
        # ... 처리 로직 ...
    }

    return {
        'status': 'success',
        'result': result
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

**핵심 요소:**
- 직접 실행을 위한 shebang 라인
- 헤더에 명확한 문서
- 주석에 사용 예시
- stdin을 통한 입력, stdout을 통한 출력
- stderr를 통한 오류
- 적절한 오류 처리
- JSON 입출력 형식
- 종료 코드(0=성공, 1=실패)

### 단계 5: 문서 업데이트

**인라인 코드를 스크립트 참조로 교체:**

**이전:**
````markdown
### 단계 3: 변경사항 수집

Git 변경사항을 수집하려면 이것을 실행:

```bash
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
git add -u

# ... bash 30줄 더 ...

echo "{"
echo "  \"branch\": \"$CURRENT_BRANCH\","
# ... JSON 출력 ...
echo "}"
```
````

**이후:**
````markdown
### 단계 3: 변경사항 수집

**필수: 미리 빌드된 스크립트 사용(bash 명령어 인라인 금지)**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh

./scripts/analysis/collect_changes.sh
```

**중요:**
- 문서에서 bash 명령어 재구성하지 마세요
- 실행 흐름에 스크립트를 인라인하지 마세요
- 항상 `EXECUTE_SCRIPT:` 지시문을 통해 미리 빌드된 스크립트 사용
- 스크립트는 0 컨텍스트 토큰을 소비(출력만 로드됨)
````

**강력한 지시문 키워드:**
- `필수:` - 필수 동작
- `EXECUTE_SCRIPT:` - 스크립트 위치 표시
- `인라인 금지` - 명시적 금지
- `항상 사용` - 강제
- `절대` - 강력한 금지
- `중요:` - 최고 우선순위
- `주목:` - 중요한 참고사항

### 단계 6: scripts/README.md 생성

**스크립트 문서용 템플릿:**

```markdown
# 스크립트 문서

[skill-name] 스킬을 위한 실행 가능한 스크립트.

**중요:** 모든 스크립트는 결정적 실행을 위해 설계됨. 에이전트는 반드시 `EXECUTE_SCRIPT:` 지시문을 사용해야 함.

---

## 디렉토리 구조

```
scripts/
├── analysis/         # 데이터 수집 및 분석
├── algorithms/       # 감지 및 의사결정 로직
├── generation/       # 콘텐츠 생성
├── validation/       # 입출력 검증
├── execution/        # 주요 작업
└── utils/           # 도우미 함수
```

---

## 스크립트

### analysis/collect_changes.sh

**목적:** 수정된 파일 자동 staging 및 Git 변경 정보 수집

**사용법:**
```bash
./scripts/analysis/collect_changes.sh
```

**입력:** 없음(Git 저장소에서 읽음)

**출력:**
```json
{
  "branch": "main",
  "stagedFiles": ["file1.js", "file2.md"],
  "modifiedFiles": ["file3.py"],
  "additions": 45,
  "deletions": 12
}
```

**종료 코드:**
- `0`: 성공
- `1`: Git 저장소가 아님
- `2`: 변경사항 감지되지 않음

---

### algorithms/detect_scope.js

**목적:** 변경된 파일에서 최적의 범위(모듈/파일/디렉토리) 감지

**사용법:**
```bash
echo '{"files": ["src/auth/login.js"]}' | node scripts/algorithms/detect_scope.js
```

**입력:**
```json
{
  "files": ["path/to/file1.js", "path/to/file2.md"]
}
```

**출력:**
```json
{
  "primary": {
    "name": "auth",
    "type": "module",
    "confidence": 0.9
  },
  "alternatives": [
    {"name": "login", "type": "file", "confidence": 0.7}
  ]
}
```

**종료 코드:**
- `0`: 성공
- `1`: 잘못된 입력 또는 처리 오류

---

[... 모든 스크립트 문서화 ...]

---

## Claude 에이전트용 스크립트 실행 지침

**중요:** 이 스크립트는 미리 빌드되고, 테스트되며, 결정적임. 에이전트는 반드시:

1. **항상 `EXECUTE_SCRIPT:` 지시문 사용** 스크립트 호출 전
2. **절대 인라인하거나 재구성하지 마세요** 문서에서 스크립트 명령어
3. **항상 스크립트를 파이프** 여러 작업을 구성할 때
4. **절대 수정하지 마세요** 스크립트 로직이나 매개변수는 명시적으로 지시되지 않는 한
5. **항상 종료 코드 확인** 다음 단계로 진행하기 전

**예시 실행 패턴:**
```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
# EXECUTE_SCRIPT: scripts/algorithms/detect_scope.js

./scripts/analysis/collect_changes.sh | \
  node scripts/algorithms/detect_scope.js > scope.json
```

---

## 스크립트 테스트

**구문 검증:**
```bash
# Bash 스크립트
bash -n scripts/**/*.sh

# JavaScript 스크립트
node -c scripts/**/*.js

# Python 스크립트
python3 -m py_compile scripts/**/*.py
```

**권한 설정:**
```bash
chmod +x scripts/**/*.{sh,js,py}
```
```

### 단계 7: 검증 및 테스트

**스크립트 검증:**

```bash
# bash 구문 확인
for script in scripts/**/*.sh; do
    bash -n "$script" || echo "$script에서 오류"
done

# JavaScript 구문 확인
for script in scripts/**/*.js; do
    node -c "$script" || echo "$script에서 오류"
done

# Python 구문 확인
for script in scripts/**/*.py; do
    python3 -m py_compile "$script" || echo "$script에서 오류"
done
```

**실행 권한 설정:**

```bash
find scripts -type f \( -name "*.sh" -o -name "*.js" -o -name "*.py" \) -exec chmod +x {} \;
```

**실행 테스트:**

```bash
# bash 스크립트 테스트
echo '{"test": "data"}' | ./scripts/category/script_name.sh

# Node.js 스크립트 테스트
echo '{"test": "data"}' | node scripts/category/script_name.js

# Python 스크립트 테스트
echo '{"test": "data"}' | python3 scripts/category/script_name.py
```

**지시문 작동 확인:**
- 새로운 대화 시작
- 스크립트로 스킬 사용
- Claude가 `EXECUTE_SCRIPT:` 지시문을 사용하는지 확인
- 인라인 코드 재구성 없음 확인

---

## 일반적인 패턴

### 패턴 1: 간단한 변환

**원래 인라인 코드:**
```bash
git status --short | awk '{print $2}' | sort
```

**스크립트로 추출:**
```bash
#!/usr/bin/env bash
# list_changed_files.sh
git status --short | awk '{print $2}' | sort
```

**문서 업데이트:**
```markdown
**EXECUTE_SCRIPT: scripts/utils/list_changed_files.sh**
```

### 패턴 2: 복잡한 알고리즘

**원래 인라인 JavaScript:**
````markdown
```javascript
function detectScope(files) {
  // 50줄 이상의 복잡한 로직
  // 여러 도우미 함수
  // 엣지 케이스 처리
  return scope;
}
```
````

**스크립트로 추출:**
```javascript
#!/usr/bin/env node
// detect_scope.js
function detectScope(files) {
  // 50줄 이상의 로직
}

// stdin/stdout 처리
if (require.main === module) {
  // ... 구현 ...
}

module.exports = { detectScope };
```

**문서 업데이트:**
````markdown
**필수: 범위 감지 스크립트 사용**

```bash
# EXECUTE_SCRIPT: scripts/algorithms/detect_scope.js
echo "$INPUT" | node scripts/algorithms/detect_scope.js
```
````

### 패턴 3: 다단계 파이프라인

**원래 인라인 코드:**
````markdown
```bash
# 단계 1: 수집
git status

# 단계 2: 변환
# ... 20줄 ...

# 단계 3: 형식화
# ... 15줄 ...

# 단계 4: 출력
# ... 10줄 ...
```
````

**스크립트로 추출:**
```bash
scripts/
├── step1_collect.sh
├── step2_transform.sh
├── step3_format.sh
└── step4_output.sh
```

**문서 업데이트:**
````markdown
**필수: 파이프라인 스크립트 사용**

```bash
# EXECUTE_SCRIPT: scripts/step1_collect.sh
# EXECUTE_SCRIPT: scripts/step2_transform.sh
# EXECUTE_SCRIPT: scripts/step3_format.sh
# EXECUTE_SCRIPT: scripts/step4_output.sh

./scripts/step1_collect.sh | \
  ./scripts/step2_transform.sh | \
  ./scripts/step3_format.sh | \
  ./scripts/step4_output.sh
```
````

---

## 모범 사례

### 스크립트 설계

**하세요:**
- ✅ 표준 I/O 사용(stdin → stdout)
- ✅ 구조화된 데이터는 JSON 출력
- ✅ 명시적으로 오류 처리
- ✅ 명확한 오류 메시지 제공
- ✅ 입출력 형식 문서화
- ✅ 종료 코드 올바르게 사용(0=성공, 0!=실패)
- ✅ 가능하면 스크립트를 멱등성 있게 만드세요

**하지 마세요:**
- ❌ 명령줄 인수 사용(stdin 사용)
- ❌ 임의의 파일에 쓰기(stdout 사용)
- ❌ 전역 상태에 의존
- ❌ 경로나 값을 하드코딩
- ❌ 조용히 실패하거나 오류 무시
- ❌ 로깅과 출력 섞기(로그는 stderr 사용)

### 문서

**하세요:**
- ✅ 강력한 지시문 키워드 사용
- ✅ `EXECUTE_SCRIPT:` 마커 추가
- ✅ 입출력 형식을 명확하게 문서화
- ✅ 사용 예시 포함
- ✅ 종료 코드 나열
- ✅ 각 스크립트를 사용할 때 설명

**하지 마세요:**
- ❌ 인라인 코드 예시 남기기
- ❌ 약한 언어 사용("you can", "consider")
- ❌ 인라인 금지를 잊기
- ❌ scripts/README.md 건결하기
- ❌ 깨진 참조 남기기
- ❌ 문서와 스크립트 섞기

### 조직

**하세요:**
- ✅ 책임별로 그룹화(분석, 알고리즘 등)
- ✅ 명확하고 설명적인 이름 사용(동사_명사)
- ✅ 스크립트 집중 유지(단일 책임)
- ✅ 디렉토리 구조 문서화
- ✅ 모든 것을 버전 관리

**하지 마세요:**
- ❌ 시간순으로 조직(단계1, 단계2)
- ❌ 모호한 이름 사용(컨텍스트 없는 helper, utils)
- ❌ 모놀리식 스크립트 만들기
- ❌ 다른 책임 섞기
- ❌ README 문서 잊기

---

## 문제 해결

### 문제: Claude가 여전히 코드를 인라인함

**진단:**
- 지시문이 충분히 강력한지 확인
- `EXECUTE_SCRIPT:` 마커가 있는지 확인
- 남은 인라인 코드 예시 찾기

**해결책:**
1. 모든 스크립트 참조에 `필수:` 추가
2. 명시적인 금지 추가: `인라인 금지`, `절대 재구성 금지`
3. ALL 인라인 코드 예시 제거(설명용이라도)
4. 언어 강화: "you should" → "항상"

### 문제: 스크립트가 권한 오류로 실패

**해결책:**
```bash
chmod +x scripts/**/*.{sh,js,py}
```

### 문제: 스크립트가 종속성을 찾을 수 없음

**해결책:**
1. 스크립트 위치에 상대적인 절대 경로 또는 상대 경로 사용:
```bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$SCRIPT_DIR/../utils/helpers.sh"
```

2. 종속성을 scripts/README.md에 문서화
3. 필요한 도구 확인(jq, node, python3)

### 문제: JSON 파싱 실패

**해결책:**
1. 스크립트에 보내기 전 JSON 검증:
```bash
echo "$DATA" | jq . > /dev/null && echo "$DATA" | ./script.sh
```

2. 스크립트에 입력 검증 추가:
```javascript
try {
  const input = JSON.parse(inputData);
} catch (e) {
  console.error('잘못된 JSON 입력');
  process.exit(1);
}
```

### 문제: 스크립트가 너무 많은 컨텍스트를 소비

**진단:**
- 스크립트는 전혀 컨텍스트에 로드되지 않아야 함
- 스크립트 OUTPUT만 컨텍스트에 나타나야 함

**해결책:**
1. 스크립트가 실제로 실행되는지 확인(읽히지 않음)
2. 문서가 스크립트를 참조하는지 확인(포함하지 않음)
3. EXECUTE_SCRIPT 마커가 명확한지 확인

---

## 지표와 영향

### 일반적인 결과

**토큰 절약:**
- 이전: 1,500-3,000 토큰(컨텍스트의 인라인 코드)
- 이후: 0 토큰(외부적으로 실행되는 스크립트)
- **절약: 100%** 스크립트 코드 토큰

**유지보수:**
- 이전: 3-5개 문서 파일 업데이트
- 이후: 1개 스크립트 파일 업데이트
- **노력: 60-80% 감소**

**일관성:**
- 이전: 50-70% 일관된 실행
- 이후: 100% 일관된 실행
- **개선: +30-50%**

### 예시: 커밋 스킬

**추출 전:**
- 인라인 코드 블록: 15개 이상
- 토큰 소비: ~2,000 토큰
- 알고리즘 변경을 위해 업데이트할 파일: 5개

**추출 후:**
- 인라인 코드 블록: 0개
- 토큰 소비: 0토큰(스크립트만)
- 알고리즘 변경을 위해 업데이트할 파일: 1개

**결과:**
- 스크립트 코드 100% 토큰 절약
- 유지보수 노력 80% 감소
- 100% 실행 일관성

---

## 체크리스트

### 계획
- [ ] 모든 인라인 코드 블록 식별
- [ ] 책임별로 분류
- [ ] 디렉토리 구조 설계
- [ ] 적절한 언어 선택

### 구현
- [ ] 적절한 템플릿으로 스크립트 파일 생성
- [ ] shebang 라인 및 문서 추가
- [ ] 오류 처리 구현
- [ ] stdin/stdout 패턴 사용
- [ ] 구조화된 JSON 출력

### 문서
- [ ] EXECUTE_SCRIPT 지시문으로 스킬 문서 업데이트
- [ ] 강력한 금지 키워드 추가
- [ ] 포괄적인 scripts/README.md 생성
- [ ] 모든 인라인 코드 예시 제거
- [ ] 입출력 형식 및 종료 코드 문서화

### 검증
- [ ] 스크립트 구문 검증
- [ ] 실행 권한 설정
- [ ] 각 스크립트를 독립적으로 테스트
- [ ] 스크립트 파이프라인 테스트
- [ ] Claude가 EXECUTE_SCRIPT 지시문을 사용하는지 확인

### 확인
- [ ] 토큰 절약 측정
- [ ] 새로운 대화에서 테스트
- [ ] 결정론적 동작 확인
- [ ] 오류 처리 확인
- [ ] 출력 형식 검증

---

## 관련 문서

- [스킬 생성](creating-skills.md) - 처음부터 스킬 구축
- [스킬 수정](modifying-skills.md) - 기존 스킬 리팩토링
