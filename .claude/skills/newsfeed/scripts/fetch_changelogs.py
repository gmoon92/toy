#!/usr/bin/env python3
"""
Claude Code Changelogs Fetcher (Hash-based Filename)

GitHub API에서 Claude Code 릴리즈 정보를 해시 기반 파일명으로 저장합니다.
- 파일명: {version}-{hash}.json (복합키의 해시)
- 복합키: (tag_name + updated_at + published_at)
- 해시 알고리즘: SHA-256 (앞 8자리)
- 멱등성: 동일한 릴리즈는 항상 동일한 파일명 생성
"""

import hashlib
import json
import os
import sys
import urllib.request
import urllib.error
from typing import List, Dict, Any, Optional, Set


# =============================================================================
# 상수 정의 (Constants)
# =============================================================================

DEFAULT_REPO = "anthropics/claude-code"
INDEX_FILENAME = "index.jsonl"
STORAGE_SUBDIR = "storage"  # JSON 파일 저장 서브디렉토리

# 저장할 필드 목록
KEYS_TO_KEEP = [
    'tag_name', 'name', 'html_url', 'target_commitish',
    'created_at', 'updated_at', 'published_at', 'body'
]

# 해싱 설정 (Hash Configuration)
HASH_ALGORITHM = 'sha256'      # 해시 알고리즘
HASH_LENGTH = 8                # 파일명에 사용할 해시 길이
HASH_KEYS = [                  # 복합키를 구성하는 필드 (순서 중요 - 멱등성)
    'tag_name',
    'updated_at',
    'published_at'
]

# 파일명 구분자
FILENAME_SEPARATOR = '-'
JSON_EXTENSION = '.json'

# API 설정
API_BASE_URL = "https://api.github.com/repos"
API_TIMEOUT_SECONDS = 30
API_ACCEPT_HEADER = 'application/vnd.github.v3+json'
API_USER_AGENT = 'Claude-Code-Changelog-Fetcher'

# 출력 설정
JSON_INDENT = 2
PRINT_WIDTH = 60

# 검증 설정 (Validation Configuration)
# API 응답에서 필수로 확인할 필드 목록
REQUIRED_PAYLOAD_KEYS = [
    'tag_name', 'name', 'html_url', 'target_commitish',
    'created_at', 'updated_at', 'published_at', 'body'
]


# =============================================================================
# 해싱 유틸리티 (Hash Utilities)
# =============================================================================

def compute_hash(release: Dict[str, Any]) -> str:
    """
    릴리즈 데이터의 복합키 해시를 계산합니다.

    복합키: HASH_KEYS에 정의된 필드를 순서대로 연결
    형식: "tag_name:value|updated_at:value|published_at:value"

    Args:
        release: GitHub API 응답의 릴리즈 데이터

    Returns:
        HASH_LENGTH 길이의 해시 문자열 (멱등성 보장)
    """
    # 복합키 생성 (필드 순서가 멱등성 보장의 핵심)
    key_parts = []
    for key in HASH_KEYS:
        value = release.get(key, '')
        key_parts.append(f"{key}:{value}")
    composite_key = '|'.join(key_parts)

    # 해시 계산
    hash_obj = hashlib.new(HASH_ALGORITHM)
    hash_obj.update(composite_key.encode('utf-8'))
    full_hash = hash_obj.hexdigest()

    return full_hash[:HASH_LENGTH]


def get_filename(release: Dict[str, Any]) -> Optional[str]:
    """
    릴리즈 데이터로부터 파일명을 생성합니다.

    형식: {tag_name}-{hash}.json
    예시: v2.1.80-a3f5c2d8.json

    Args:
        release: GitHub API 응답의 릴리즈 데이터

    Returns:
        생성된 파일명 또는 None (tag_name 없음)
    """
    tag_name = release.get('tag_name')
    if not tag_name:
        return None

    hash_value = compute_hash(release)
    return f"{tag_name}{FILENAME_SEPARATOR}{hash_value}.json"


def extract_version_from_filename(filename: str) -> Optional[str]:
    """파일명에서 버전을 추출합니다."""
    if not filename.endswith(JSON_EXTENSION) or filename == INDEX_FILENAME:
        return None

    name_without_ext = filename[:-len(JSON_EXTENSION)]
    parts = name_without_ext.rsplit(FILENAME_SEPARATOR, 1)

    return parts[0] if len(parts) == 2 else None


# =============================================================================
# 인덱스 관리 (Index Management)
# =============================================================================

def get_storage_dir(output_dir: str) -> str:
    """
    JSON 파일 저장용 storage 디렉토리 경로를 반환합니다.

    Args:
        output_dir: 루트 출력 디렉토리

    Returns:
        storage 서브디렉토리 경로
    """
    return os.path.join(output_dir, STORAGE_SUBDIR)


def load_index(output_dir: str) -> Dict[str, str]:
    """
    index.jsonl을 로드하여 {version: filename} 형태로 반환합니다.

    Args:
        output_dir: index.jsonl 파일이 있는 디렉토리

    Returns:
        {버전: 파일명} 형태의 딕셔너리
    """
    index_path = os.path.join(output_dir, INDEX_FILENAME)
    index = {}

    if not os.path.exists(index_path):
        return index

    try:
        with open(index_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    entry = json.loads(line)
                    version = entry.get('version')
                    filename = entry.get('filename')
                    if version and filename:
                        index[version] = filename
                except json.JSONDecodeError:
                    continue
    except (IOError, OSError):
        pass

    return index


def sync_index_with_filesystem(output_dir: str, latest_version: Optional[str] = None) -> None:
    """
    파일 시스템 상태를 기준으로 index를 동기화합니다.

    Args:
        output_dir: 출력 디렉토리
        latest_version: 최신 버전 (API에서 조회한 최신 버전)
    """
    print("\nSyncing index with filesystem...")
    index = build_index_from_files(output_dir)
    save_index(index, output_dir, latest_version)


def save_index(index: Dict[str, str], output_dir: str, latest_version: Optional[str] = None) -> None:
    """
    index를 index.jsonl로 저장합니다.

    Args:
        index: {버전: 파일명} 형태의 딕셔너리
        output_dir: 저장할 디렉토리
        latest_version: 최신 버전 (API에서 조회한 최신 버전)
    """
    index_path = os.path.join(output_dir, INDEX_FILENAME)

    with open(index_path, 'w', encoding='utf-8') as f:
        for version, filename in sorted(index.items(), reverse=True):
            entry = {
                'version': version,
                'filename': filename,
                'latest': version == latest_version
            }
            f.write(json.dumps(entry, ensure_ascii=False) + '\n')


def build_index_from_files(output_dir: str) -> Dict[str, str]:
    """
    파일 시스템을 스캔하여 index를 재구성합니다.

    Args:
        output_dir: 출력 디렉토리 경로

    Returns:
        파일 시스템 기반의 {버전: 파일명} 딕셔너리
    """
    index = {}
    storage_dir = get_storage_dir(output_dir)

    if not os.path.exists(storage_dir):
        return index

    for filename in os.listdir(storage_dir):
        version = extract_version_from_filename(filename)
        if version:
            index[version] = filename

    return index


# =============================================================================
# 파일 상태 확인 (File Status Check)
# =============================================================================

def get_local_versions(output_dir: str) -> Set[str]:
    """
    로컬에 존재하는 모든 버전을 반환합니다.

    Args:
        output_dir: 출력 디렉토리

    Returns:
        버전 문자열의 집합
    """
    versions = set()
    storage_dir = get_storage_dir(output_dir)

    if not os.path.exists(storage_dir):
        return versions

    for filename in os.listdir(storage_dir):
        version = extract_version_from_filename(filename)
        if version:
            versions.add(version)

    return versions


def is_file_up_to_date(release: Dict[str, Any], output_dir: str) -> bool:
    """
    릴리즈가 로컬에 최신 상태로 존재하는지 확인합니다.

    파일명에 해시가 포함되어 있으므로, 파일 존재 여부만 확인하면 됩니다.

    Args:
        release: GitHub API 응답의 릴리즈 데이터
        output_dir: 출력 디렉토리

    Returns:
        파일이 존재하면 True (해시 일치), 없으면 False
    """
    filename = get_filename(release)
    if not filename:
        return False

    storage_dir = get_storage_dir(output_dir)
    filepath = os.path.join(storage_dir, filename)
    return os.path.exists(filepath)


# =============================================================================
# API 검증 (API Validation)
# =============================================================================

class APIValidationError(Exception):
    """API 응답 검증 실패 시 발생하는 예외"""
    pass


def validate_api_response(release: Dict[str, Any], context: str = "API response") -> List[str]:
    """
    API 응답에 필요한 모든 필드가 있는지 검증합니다.

    Args:
        release: GitHub API 응답 데이터
        context: 검증 컨텍스트 (에러 메시지용)

    Returns:
        누락된 필드 목록 (없으면 빈 리스트)
    """
    missing_keys = []
    for key in REQUIRED_PAYLOAD_KEYS:
        if key not in release:
            missing_keys.append(key)
    return missing_keys


def validate_hash_keys(release: Dict[str, Any]) -> List[str]:
    """
    해싱에 필요한 필드가 모두 있는지 검증합니다.

    Args:
        release: GitHub API 응답 데이터

    Returns:
        누락된 해시 키 목록 (없으면 빈 리스트)
    """
    missing_keys = []
    for key in HASH_KEYS:
        if key not in release or not release.get(key):
            missing_keys.append(key)
    return missing_keys


def format_validation_error(missing_keys: List[str], hash_keys: List[str]) -> str:
    """
    검증 오류 메시지를 포맷팅합니다.

    Args:
        missing_keys: 누락된 일반 필드 목록
        hash_keys: 누락된 해시 필드 목록

    Returns:
        사용자 친화적인 에러 메시지
    """
    lines = ["\n" + "=" * 60]
    lines.append("⚠️  GitHub API 응답 구조가 변경되었습니다!")
    lines.append("=" * 60)
    lines.append("")
    lines.append("스크립트가 예상하는 필드가 API 응답에 없습니다.")
    lines.append("")

    if missing_keys:
        lines.append("[누락된 일반 필드]")
        for key in missing_keys:
            lines.append(f"  - {key}")
        lines.append("")

    if hash_keys:
        lines.append("[누락된 해시 필드 - 파일명 생성 불가]")
        for key in hash_keys:
            lines.append(f"  - {key}")
        lines.append("")

    lines.append("[조치 방법]")
    lines.append("  1. GitHub API 문서를 확인하세요:")
    lines.append("     https://docs.github.com/en/rest/releases/releases")
    lines.append("  2. 스크립트의 KEYS_TO_KEEP와 HASH_KEYS 상수를")
    lines.append("     새로운 API 응답 구조에 맞게 업데이트하세요.")
    lines.append("  3. 업데이트 후 스크립트를 다시 실행하세요.")
    lines.append("=" * 60)

    return "\n".join(lines)


# =============================================================================
# GitHub API (GitHub API)
# =============================================================================

def create_api_request(url: str) -> urllib.request.Request:
    """GitHub API 요청 객체를 생성합니다."""
    return urllib.request.Request(
        url,
        headers={
            'Accept': API_ACCEPT_HEADER,
            'User-Agent': API_USER_AGENT
        }
    )


def fetch_json_from_api(url: str) -> Optional[Any]:
    """API에서 JSON 데이터를 가져옵니다."""
    try:
        req = create_api_request(url)
        with urllib.request.urlopen(req, timeout=API_TIMEOUT_SECONDS) as response:
            return json.loads(response.read().decode('utf-8'))
    except Exception:
        return None


def fetch_latest_release() -> Optional[Dict[str, Any]]:
    """GitHub API에서 최신 릴리즈를 조회합니다."""
    url = f"{API_BASE_URL}/{DEFAULT_REPO}/releases/latest"
    result = fetch_json_from_api(url)
    return result if isinstance(result, dict) else None


def fetch_releases_page(page: int, per_page: int = DEFAULT_PER_PAGE) -> List[Dict[str, Any]]:
    """GitHub API에서 특정 페이지의 릴리즈 목록을 조회합니다."""
    url = f"{API_BASE_URL}/{DEFAULT_REPO}/releases?per_page={per_page}&page={page}"
    result = fetch_json_from_api(url)
    return result if isinstance(result, list) else []


# =============================================================================
# 파일 저장 (File Save)
# =============================================================================

def save_json_file(data: Dict[str, Any], filepath: str) -> bool:
    """JSON 데이터를 파일로 저장합니다."""
    try:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=JSON_INDENT, ensure_ascii=False)
        return True
    except (IOError, OSError):
        return False


def save_release(release: Dict[str, Any], output_dir: str) -> Optional[str]:
    """릴리즈 데이터를 JSON 파일로 저장합니다."""
    filename = get_filename(release)
    if not filename:
        return None

    storage_dir = get_storage_dir(output_dir)
    os.makedirs(storage_dir, exist_ok=True)

    filtered_data = {k: release.get(k) for k in KEYS_TO_KEEP if k in release}
    filepath = os.path.join(storage_dir, filename)

    return filename if save_json_file(filtered_data, filepath) else None


def save_releases_batch(
    releases: List[Dict[str, Any]],
    output_dir: str
) -> tuple[List[str], List[str]]:
    """
    여러 릴리즈를 일괄 저장합니다.

    Args:
        releases: 저장할 릴리즈 목록
        output_dir: 출력 디렉토리

    Returns:
        (저장된 파일명 목록, 저장된 버전 목록) 튜플
    """
    print(f"\nSaving {len(releases)} releases...")
    saved_filenames: List[str] = []
    saved_versions: List[str] = []

    for release in releases:
        filename = save_release(release, output_dir)
        if filename:
            saved_filenames.append(filename)
            tag = release.get('tag_name')
            if tag:
                saved_versions.append(tag)
            print(f"  Saved: {tag} ({filename})")

    return saved_filenames, saved_versions


# =============================================================================
# 결과 응답 구조 (Response Structure)
# =============================================================================

def format_text_output(result: 'FetchResult') -> str:
    """FetchResult를 사용자 친화적인 텍스트 형식으로 변환합니다."""
    lines = [
        "",
        "=" * PRINT_WIDTH,
        f"결과: {'성공' if result.success else '실패'}",
        f"메시지: {result.message}",
    ]

    if result.error_code:
        lines.append(f"에러 코드: {result.error_code}")

    lines.extend([
        f"저장된 릴리즈: {result.total_saved}",
        f"API 호출 수: {result.api_calls}",
    ])

    if result.saved_versions:
        lines.append(f"저장된 버전: {', '.join(result.saved_versions[:5])}")
        if len(result.saved_versions) > 5:
            lines.append(f"  ... 외 {len(result.saved_versions) - 5}개")

    lines.append("=" * PRINT_WIDTH)

    return "\n".join(lines)


def create_api_connection_error(api_calls: int) -> 'FetchResult':
    """API 연결 실패 결과를 생성합니다."""
    error_msg = """
============================================================
❌ GitHub API 호출 실패
============================================================
GitHub API에 연결할 수 없습니다.

[조치 방법]
  1. 인터넷 연결을 확인하세요.
  2. GitHub API 상태를 확인하세요: https://www.githubstatus.com/
  3. rate limit 초과 여부를 확인하세요.
============================================================
"""
    print(error_msg)
    return FetchResult(
        success=False,
        message="GitHub API 연결 실패",
        error_code=FetchResult.ERROR_API_CONNECTION,
        error_details={
            'reason': 'Failed to fetch latest release',
            'suggestions': [
                'Check internet connection',
                'Verify GitHub API status',
                'Check rate limit status'
            ]
        },
        api_calls=api_calls
    )


def create_success_result(
    message: str,
    api_calls: int,
    total_saved: int = 0,
    saved_versions: Optional[List[str]] = None
) -> 'FetchResult':
    """성공 결과를 생성합니다."""
    return FetchResult(
        success=True,
        message=message,
        api_calls=api_calls,
        total_saved=total_saved,
        saved_versions=saved_versions or []
    )


def create_validation_error(
    message: str,
    missing_keys: List[str],
    missing_hash_keys: List[str],
    api_calls: int,
    total_saved: int = 0,
    saved_versions: Optional[List[str]] = None,
    release_tag: Optional[str] = None
) -> 'FetchResult':
    """API 검증 실패 결과를 생성합니다."""
    error_msg = format_validation_error(missing_keys, missing_hash_keys)
    print(error_msg)

    error_details = {
        'missing_keys': missing_keys,
        'missing_hash_keys': missing_hash_keys,
        'required_keys': REQUIRED_PAYLOAD_KEYS,
        'required_hash_keys': HASH_KEYS,
        'suggestions': [
            'Check GitHub API documentation',
            'Update KEYS_TO_KEEP and HASH_KEYS constants',
            'Re-run the script after updates'
        ]
    }

    if release_tag:
        error_details['release_tag'] = release_tag

    return FetchResult(
        success=False,
        message=message,
        error_code=FetchResult.ERROR_API_VALIDATION,
        error_details=error_details,
        total_saved=total_saved,
        api_calls=api_calls,
        saved_versions=saved_versions or []
    )


class FetchResult:
    """
    스크립트 실행 결과를 표준화된 형태로 반환하는 클래스

    Attributes:
        success (bool): 실행 성공 여부
        message (str): 사용자 친화적인 결과 메시지
        error_code (str | None): 에러 식별 코드 (실패 시)
        error_details (dict | None): 상세 에러 정보 (실패 시)
        total_saved (int): 저장된 릴리즈 수
        api_calls (int): 수행된 API 호출 수
        saved_versions (list): 저장된 버전 목록
    """

    # 에러 코드 정의
    ERROR_API_CONNECTION = "API_CONNECTION_FAILED"
    ERROR_API_VALIDATION = "API_VALIDATION_FAILED"
    ERROR_FILE_SYSTEM = "FILE_SYSTEM_ERROR"
    ERROR_UNKNOWN = "UNKNOWN_ERROR"

    def __init__(
        self,
        success: bool,
        message: str,
        error_code: Optional[str] = None,
        error_details: Optional[Dict[str, Any]] = None,
        total_saved: int = 0,
        api_calls: int = 0,
        saved_versions: Optional[List[str]] = None
    ):
        self.success = success
        self.message = message
        self.error_code = error_code
        self.error_details = error_details or {}
        self.total_saved = total_saved
        self.api_calls = api_calls
        self.saved_versions = saved_versions or []

    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리 형태로 변환"""
        return {
            'success': self.success,
            'message': self.message,
            'error_code': self.error_code,
            'error_details': self.error_details,
            'total_saved': self.total_saved,
            'api_calls': self.api_calls,
            'saved_versions': self.saved_versions
        }

    def __repr__(self) -> str:
        status = "✓ SUCCESS" if self.success else "✗ FAILED"
        return f"[{status}] {self.message} (saved: {self.total_saved}, api_calls: {self.api_calls})"


# =============================================================================
# 메인 로직 (Main Logic)
# =============================================================================

def _process_single_release(
    release: Dict[str, Any],
    output_dir: str,
    local_versions: Set[str],
    to_save: List[Dict[str, Any]],
    api_calls: int
) -> Optional[FetchResult]:
    """
    단일 릴리즈를 처리하고 필요시 에러 결과를 반환합니다.

    Args:
        release: 처리할 릴리즈 데이터
        output_dir: 출력 디렉토리
        local_versions: 로컬에 존재하는 버전 집합
        to_save: 저장할 릴리즈 목록 (수정됨)
        api_calls: 현재까지의 API 호출 수

    Returns:
        에러 발생 시 FetchResult, 정상 처리 시 None
    """
    tag = release.get('tag_name')
    if not tag:
        return None

    # 각 릴리즈의 필수 필드 검증
    missing_keys = validate_api_response(release, f"release {tag}")
    missing_hash_keys = validate_hash_keys(release)

    if missing_keys or missing_hash_keys:
        return create_validation_error(
            message=f"릴리즈 {tag}의 API 응답 구조 변경 감지",
            missing_keys=missing_keys,
            missing_hash_keys=missing_hash_keys,
            api_calls=api_calls,
            total_saved=len(to_save),
            saved_versions=[r.get('tag_name') for r in to_save if r.get('tag_name')],
            release_tag=tag
        )

    filename = get_filename(release)
    if not filename:
        return None

    # 파일 존재 확인 (파일명에 해시가 포함되어 있으므로 복합키 일치도 동시에 확인)
    if is_file_up_to_date(release, output_dir):
        print(f"  Skipped: {tag} (up to date: {filename})")
        return None

    # 파일 없음 → 갱신 필요
    to_save.append(release)
    status = "New" if tag not in local_versions else "Updated"
    print(f"  {status}: {tag} ({filename})")
    return None


def fetch_changelogs(output_dir: str, per_page: int = 100) -> FetchResult:
    """
    해시 기반 파일명으로 릴리즈 정보를 가져옵니다.

    로직:
    1. 사전 API 검증 (Pre-flight check)
    2. 최신 릴리즈 API 호출
    3. 파일 존재 확인 (파일명에 해시가 포함되어 있으므로 별도 비교 불필요)
    4. 탑다운 순회: 필요한 버전만 저장
    5. index 동기화

    Returns:
        FetchResult: 표준화된 실행 결과 객체
    """
    os.makedirs(output_dir, exist_ok=True)

    api_calls = 0
    to_save = []

    # 0단계: 사전 API 검증 (Pre-flight check)
    print("Validating API response structure...")
    validation_release = fetch_latest_release()
    api_calls += 1

    if not validation_release:
        return create_api_connection_error(api_calls)

    # 모든 페이로드 키 검증
    missing_keys = validate_api_response(validation_release, "latest release")
    missing_hash_keys = validate_hash_keys(validation_release)

    if missing_keys or missing_hash_keys:
        return create_validation_error(
            message="GitHub API 응답 구조 변경 감지",
            missing_keys=missing_keys,
            missing_hash_keys=missing_hash_keys,
            api_calls=api_calls
        )

    print("  ✓ API response structure is valid")

    # 1단계: 최신 릴리즈 조회 (검증용으로 이미 가져옴)
    print("\nFetching latest release...")
    latest = validation_release
    latest_tag = latest.get('tag_name')
    latest_filename = get_filename(latest)
    print(f"  Latest: {latest_tag} ({latest_filename})")

    # 2단계: Early Return 검토
    # 최신 버전의 파일이 존재하는지 확인 (파일명에 해시가 포함되어 있으므로 일치 여부도 동시에 확인)
    local_versions = get_local_versions(output_dir)

    if is_file_up_to_date(latest, output_dir):
        # 최신 버전 파일이 존재하면, 전체 버전 수도 확인
        if len(local_versions) > 0:
            print("  Already up to date")
            # index 동기화
            sync_index_with_filesystem(output_dir, latest_tag)
            return create_success_result(
                message="이미 최신 상태입니다",
                api_calls=api_calls
            )

    # 3단계: 탑다운 순회하며 갱신 필요 버전 탐지
    print("\nScanning for updates...")
    page = 1

    while True:
        releases = fetch_releases_page(page, per_page)
        api_calls += 1

        if not releases:
            break

        for release in releases:
            result = _process_single_release(
                release, output_dir, local_versions, to_save, api_calls
            )
            if result:
                return result

        if len(releases) < per_page:
            break

        page += 1

    # 4단계: 저장
    saved_filenames, saved_versions = save_releases_batch(to_save, output_dir)

    # 5단계: index 동기화
    sync_index_with_filesystem(output_dir, latest_tag)

    print(f"\nDone! Saved: {len(saved_filenames)}, API calls: {api_calls}")

    return create_success_result(
        message=f"{len(saved_filenames)}개의 릴리즈를 성공적으로 저장했습니다",
        api_calls=api_calls,
        total_saved=len(saved_filenames),
        saved_versions=saved_versions
    )


def create_argument_parser() -> 'argparse.ArgumentParser':
    """CLI argument parser를 생성합니다."""
    import argparse

    parser = argparse.ArgumentParser(
        description='Fetch Claude Code changelogs (hash-based filename)'
    )
    parser.add_argument('output_dir', help='Directory to save changelog JSON files')
    parser.add_argument(
        '--per-page',
        type=int,
        default=DEFAULT_PER_PAGE,
        help=f'Number of releases per page (default: {DEFAULT_PER_PAGE})'
    )
    parser.add_argument(
        '--json-output',
        action='store_true',
        help='Output result as JSON for programmatic use'
    )
    return parser


def output_result(result: 'FetchResult', json_output: bool = False) -> None:
    """결과를 출력합니다."""
    if json_output:
        print(json.dumps(result.to_dict(), ensure_ascii=False, indent=2))
    else:
        print(format_text_output(result))


def main():
    """CLI entry point"""
    parser = create_argument_parser()
    args = parser.parse_args()
    output_dir = os.path.abspath(args.output_dir)

    result = fetch_changelogs(output_dir=output_dir, per_page=args.per_page)
    output_result(result, json_output=args.json_output)

    # Exit code: 0 = 성공, 1 = 실패 (int 형식)
    sys.exit(0 if result.success else 1)


if __name__ == '__main__':
    main()
