GitHub API에서 Claude Code 릴리즈 정보를 가져와 버전별 JSON 파일로 저장합니다.

## Workflow

{output-dir} = .claude/skills/claudecode-document-validator/changelogs

1. 스크립트 파일이 존재하는지 확인합니다.
    ```bash
    ls -la .claude/skills/claudecode-document-validator/scripts/fetch_changelogs.py
    ```
2. Python 스크립트를 실행하여 changelogs를 가져옵니다.
    - 작업: 스크립트 실행
        ```bash
        python3 .claude/skills/claudecode-document-validator/scripts/fetch_changelogs.py {output-dir} --json-output
        ```
    - 출력: 실행 결과 JSON 출력
3. 실행 결과 검증
    - JSON 출력의 `success` 필드를 확인합니다.
    - `success`가 `false`인 경우 작업을 중단합니다.
    - `success`가 `true`인 경우에만 다음 단계로 진행합니다.
4. 디렉토리 및 파일 결과 확인
    ```bash
    # storage 디렉토리 존재 여부 확인
    ls -la {output-dir}/storage/

    # 생성된 JSON 파일 수 확인 (storage 디렉토리 내)
    ls {output-dir}/storage/*.json | wc -l

    # index.jsonl 파일 존재 여부 확인
    ls -la {output-dir}/index.jsonl

    # 최신 버션 파일 예시 확인
    head -5 {output-dir}/index.jsonl
    ```
5. 결과 요약 출력
    - 저장된 릴리즈 수: `total_saved` 값
    - API 호출 수: `api_calls` 값
    - 저장된 버전 목록: `saved_versions` 값 (있는 경우)
