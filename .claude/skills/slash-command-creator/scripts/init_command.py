#!/usr/bin/env python3
"""
슬래시 커맨드 초기화 스크립트 - 템플릿으로부터 새 슬래시 커맨드 파일을 생성합니다.

사용법:
    init_command.py <command-name> [--scope project|personal] [--path <custom-path>]

예시:
    init_command.py review                    # .claude/commands/review.md 생성
    init_command.py deploy --scope personal   # ~/.claude/commands/deploy.md 생성
    init_command.py test --path ./my-commands # ./my-commands/test.md 생성
"""

import sys
import os
from pathlib import Path
import argparse


def title_case(name: str) -> str:
    """하이픈으로 구분된 이름을 Title Case로 변환합니다."""
    return ' '.join(word.capitalize() for word in name.replace('-', ' ').split())


def get_project_commands_path() -> Path:
    """프로젝트 레벨 커맨드 디렉토리 경로를 반환합니다."""
    return Path.cwd() / '.claude' / 'commands'


def get_personal_commands_path() -> Path:
    """사용자 레벨(개인) 커맨드 디렉토리 경로를 반환합니다."""
    return Path.home() / '.claude' / 'commands'


def build_content(
    command_title: str,
    description: str,
    body: str,
    allowed_tools: str = None,
    argument_hint: str = None,
    model: str = None,
    disable_model_invocation: bool = False,
) -> str:
    """커맨드 파일 내용을 동적 frontmatter와 함께 조립합니다.

    명시적으로 전달된 필드만 frontmatter에 포함하므로,
    선택 필드를 임의 조합해도 올바르게 동작합니다.
    """
    # --- 단계 1: frontmatter 줄 목록 조립 ---
    # 알파벳 순으로 정렬하여 일관된 파일 형식 유지
    frontmatter_lines = ['---']
    if allowed_tools:
        frontmatter_lines.append(f'allowed-tools: {allowed_tools}')
    if argument_hint:
        frontmatter_lines.append(f'argument-hint: {argument_hint}')
    frontmatter_lines.append(f'description: {description}')
    if disable_model_invocation:
        frontmatter_lines.append('disable-model-invocation: true')
    if model:
        frontmatter_lines.append(f'model: {model}')
    frontmatter_lines.append('---')

    # --- 단계 2: frontmatter + 제목 + 본문 합치기 ---
    frontmatter = '\n'.join(frontmatter_lines)
    return f'{frontmatter}\n\n# {command_title}\n\n{body}\n'


def init_command(
    command_name: str,
    scope: str = 'project',
    custom_path: str = None,
    description: str = None,
    body: str = None,
    allowed_tools: str = None,
    argument_hint: str = None,
    model: str = None,
    namespace: str = None,
    disable_model_invocation: bool = False,
) -> Path:
    """
    새 슬래시 커맨드 파일을 초기화합니다.

    Args:
        command_name: 커맨드 이름 (.md 확장자 제외)
        scope: 'project'(.claude/commands) 또는 'personal'(~/.claude/commands)
        custom_path: 경로 직접 지정 시 사용 (scope 무시)
        description: 커맨드 설명 (/help에 표시)
        body: 커맨드 본문 (프롬프트 내용)
        allowed_tools: 허용할 도구 목록 (쉼표 구분)
        argument_hint: 인수 힌트 (자동완성에 표시)
        model: 사용할 모델 ID
        namespace: 하위 디렉토리 네임스페이스
        disable_model_invocation: SlashCommand 도구로 호출 방지 여부

    Returns:
        생성된 커맨드 파일 경로, 실패 시 None
    """
    # --- 단계 1: 저장 경로 결정 ---
    # custom_path > personal scope > project scope 순으로 우선순위 적용
    if custom_path:
        base_path = Path(custom_path).resolve()
    elif scope == 'personal':
        base_path = get_personal_commands_path()
    else:
        base_path = get_project_commands_path()

    # --- 단계 2: 네임스페이스 하위 디렉토리 적용 ---
    # 예: --namespace frontend → .claude/commands/frontend/
    if namespace:
        base_path = base_path / namespace

    # --- 단계 3: 디렉토리 생성 ---
    # 중간 경로가 없어도 한 번에 생성 (parents=True)
    try:
        base_path.mkdir(parents=True, exist_ok=True)
    except Exception as e:
        print(f"디렉토리 생성 실패 {base_path}: {e}")
        return None

    # --- 단계 4: 파일 경로 확정 및 중복 확인 ---
    command_file = base_path / f'{command_name}.md'
    if command_file.exists():
        print(f"오류: 이미 존재하는 커맨드입니다: {command_file}")
        return None

    # --- 단계 5: 기본값으로 내용 준비 ---
    # description/body 미전달 시 TODO 플레이스홀더 삽입
    command_title = title_case(command_name)
    description = description or f"[TODO: /{command_name} 에 대한 짧은 설명]"
    body = body or f"[TODO: /{command_name} 의 프롬프트 내용을 작성하세요]\n\n$ARGUMENTS"

    # --- 단계 6: 파일 내용 조립 ---
    content = build_content(
        command_title=command_title,
        description=description,
        body=body,
        allowed_tools=allowed_tools,
        argument_hint=argument_hint,
        model=model,
        disable_model_invocation=disable_model_invocation,
    )

    # --- 단계 7: 파일 기록 ---
    try:
        command_file.write_text(content)
        print(f"생성됨: {command_file}")
        return command_file
    except Exception as e:
        print(f"파일 쓰기 실패: {e}")
        return None


def main():
    parser = argparse.ArgumentParser(
        description='새 Claude Code 슬래시 커맨드를 초기화합니다.',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog='''
예시:
  %(prog)s review
      .claude/commands/review.md 생성 (프로젝트 커맨드)

  %(prog)s deploy --scope personal
      ~/.claude/commands/deploy.md 생성 (개인 커맨드)

  %(prog)s component --namespace frontend
      .claude/commands/frontend/component.md 생성

  %(prog)s commit --allowed-tools "Bash(git add:*), Bash(git commit:*)"
      특정 도구 권한을 가진 커맨드 생성

  %(prog)s review-pr --argument-hint "[pr-number] [priority]"
      인수 힌트가 있는 커맨드 생성

  %(prog)s analyze --model claude-haiku-4-5-20251001
      특정 모델을 사용하는 커맨드 생성

커맨드 이름 규칙:
  - 소문자와 하이픈 사용 (예: 'review-pr', 'run-tests')
  - 공백 및 특수문자 사용 금지
  - 간결하고 의미 있는 이름 선택
'''
    )

    parser.add_argument('command_name', help='슬래시 커맨드 이름 (.md 제외)')
    parser.add_argument('--scope', choices=['project', 'personal'], default='project',
                        help='커맨드 스코프: project(.claude/commands) 또는 personal(~/.claude/commands)')
    parser.add_argument('--path', dest='custom_path', help='커맨드 파일 저장 경로 직접 지정')
    parser.add_argument('--namespace', help='하위 디렉토리 네임스페이스 (예: frontend, backend)')
    parser.add_argument('--description', help='커맨드 설명 (/help에 표시됨)')
    parser.add_argument('--body', help='커맨드 본문 (프롬프트 내용)')
    parser.add_argument('--allowed-tools', dest='allowed_tools', help='허용할 도구 목록 (쉼표 구분)')
    parser.add_argument('--argument-hint', dest='argument_hint', help='인수 힌트 (자동완성에 표시됨)')
    parser.add_argument('--model', help='사용할 모델 ID (예: claude-haiku-4-5-20251001)')
    parser.add_argument('--disable-model-invocation', dest='disable_model_invocation',
                        action='store_true', default=False,
                        help='SlashCommand 도구로 프로그래매틱 호출 방지')

    args = parser.parse_args()

    result = init_command(
        command_name=args.command_name,
        scope=args.scope,
        custom_path=args.custom_path,
        description=args.description,
        body=args.body,
        allowed_tools=args.allowed_tools,
        argument_hint=args.argument_hint,
        model=args.model,
        namespace=args.namespace,
        disable_model_invocation=args.disable_model_invocation,
    )

    if result:
        print(f"\n슬래시 커맨드 '/{args.command_name}' 초기화 완료!")
        print("\n다음 단계:")
        print("1. 커맨드 파일을 열어 description과 본문을 수정하세요")
        print("2. Claude Code에서 커맨드를 실행하여 테스트하세요")
        print(f"\n사용법: /{args.command_name} [인수]")
        sys.exit(0)
    else:
        sys.exit(1)


if __name__ == "__main__":
    main()
