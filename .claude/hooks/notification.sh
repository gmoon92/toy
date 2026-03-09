#!/bin/bash
# Claude Code Notification Hook
# macOS/Linux/Windows 자동 감지 및 알림 전송

TITLE="Claude Code"
MESSAGE="Claude Code needs your attention"

# OS 감지 및 알림 전송
detect_os_and_notify() {
    case "$(uname -s)" in
        Darwin*)
            # macOS
            if command -v osascript >/dev/null 2>&1; then
                osascript -e "display notification \"$MESSAGE\" with title \"$TITLE\""
            else
                echo "osascript not found" >&2
                exit 1
            fi
            ;;
        Linux*)
            # Linux - notify-send (DBus session bus 처리)
            if command -v notify-send >/dev/null 2>&1; then
                # DBUS_SESSION_BUS_ADDRESS가 설정되지 않은 경우 찾기
                if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
                    # 현재 사용자의 DBus 세션 주소 찾기
                    USER_ID=$(id -u)
                    if [ -S "/run/user/$USER_ID/bus" ]; then
                        export DBUS_SESSION_BUS_ADDRESS="unix:path=/run/user/$USER_ID/bus"
                    elif [ -f "$HOME/.dbus/session-bus/$(cat /etc/machine-id 2>/dev/null || hostname)-$(echo $DISPLAY | cut -d: -f1)" ]; then
                        DBUS_FILE="$HOME/.dbus/session-bus/$(cat /etc/machine-id 2>/dev/null || hostname)-$(echo $DISPLAY | cut -d: -f1)"
                        if [ -f "$DBUS_FILE" ]; then
                            source "$DBUS_FILE"
                        fi
                    fi
                fi
                notify-send "$TITLE" "$MESSAGE"
            elif command -v zenity >/dev/null 2>&1; then
                # notify-send가 없으면 zenity로 대체 (GUI 환경에서만)
                zenity --info --title="$TITLE" --text="$MESSAGE" 2>/dev/null || true
            else
                echo "notify-send or zenity not found" >&2
                exit 1
            fi
            ;;
        CYGWIN*|MINGW*|MSYS*)
            # Windows (Git Bash, Cygwin, MSYS2)
            if command -v powershell.exe >/dev/null 2>&1; then
                powershell.exe -Command "
                    [System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms') | Out-Null
                    [System.Windows.Forms.MessageBox]::Show('$MESSAGE', '$TITLE')
                " 2>/dev/null || true
            else
                echo "PowerShell not found" >&2
                exit 1
            fi
            ;;
        *)
            # Unknown OS
            echo "Unsupported OS: $(uname -s)" >&2
            exit 1
            ;;
    esac
}

# 메인 실행
detect_os_and_notify
exit 0
