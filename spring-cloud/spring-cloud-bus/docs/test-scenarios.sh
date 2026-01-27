#!/bin/bash

###############################################################################
# Spring Cloud Bus 테스트 시나리오
#
# 이 스크립트는 설정 갱신과 커스텀 이벤트를 포함한
# Spring Cloud Bus 기능에 대한 자동화된 테스트 시나리오를 제공합니다.
#
# 사전 요구사항:
# - localhost:5672에서 실행 중인 RabbitMQ
# - localhost:8888에서 실행 중인 Config Server
# - localhost:8080, 8081, 8082에서 실행 중인 Config Client 인스턴스들
# - Config Server의 Native 파일 시스템 설정 (Git 불필요)
#
# 사용법:
#   chmod +x test-scenarios.sh
#   ./test-scenarios.sh [scenario_number]
#
# 사용 가능한 시나리오:
#   1 - Bus를 통한 설정 갱신 테스트
#   2 - 커스텀 이벤트 브로드캐스팅 테스트
#   3 - 특정 인스턴스 갱신 테스트
#   4 - 전체 통합 테스트
###############################################################################

set -e

# 출력 색상
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정
CONFIG_SERVER="http://localhost:8888"
CLIENT_8080="http://localhost:8080"
CLIENT_8081="http://localhost:8081"
CLIENT_8082="http://localhost:8082"
# Native 파일 시스템 경로 (프로젝트 기준)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_REPO="$SCRIPT_DIR/../spring-cloud-bus-server/src/main/resources/config-repo/alpha"

###############################################################################
# 헬퍼 함수들
###############################################################################

print_header() {
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  $1${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════╝${NC}"
}

print_step() {
    echo -e "${GREEN}▶ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0

    print_step "$service_name이(가) 준비될 때까지 대기 중..."

    while ! curl -s "$url/actuator/health" > /dev/null; do
        attempt=$((attempt + 1))
        if [ $attempt -eq $max_attempts ]; then
            print_error "$service_name이(가) 응답하지 않습니다"
            return 1
        fi
        sleep 1
    done

    print_success "$service_name이(가) 준비되었습니다"
}

get_config() {
    local port=$1
    local url="http://localhost:$port/config"
    echo -e "${YELLOW}인스턴스 :$port의 설정${NC}"
    curl -s "$url" | jq '.configProperties'
    echo ""
}

###############################################################################
# 시나리오 1: Bus를 통한 설정 갱신 테스트
###############################################################################

test_config_refresh() {
    print_header "시나리오 1: Bus를 통한 설정 갱신"

    # Step 1: 현재 설정 확인
    print_step "Step 1: 모든 인스턴스의 현재 설정 확인 중..."
    get_config 8080
    get_config 8081
    get_config 8082

    # Step 2: Native 파일 시스템의 설정 업데이트
    print_step "Step 2: 설정 파일 업데이트 중 (Git 커밋 불필요)..."

    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    cat > "$CONFIG_REPO/client.yml" << EOF
# Config Server에서 관리하는 spring-configserver-client 애플리케이션 설정 파일
# 이 파일을 수정한 후 POST /actuator/busrefresh를 호출하면 재시작 없이 설정이 갱신됩니다.
app:
  message: "Updated via Bus Refresh - ${timestamp}"
  version: "alpha 2.0.0"
  feature:
    enabled: true
    max-retry: 5
    timeout-seconds: 60

database:
  pool:
    min-size: 10
    max-size: 50
    connection-timeout: 3000
EOF

    print_success "설정이 업데이트되었습니다 (파일 저장만으로 완료)"

    # Step 3: 갱신 전 대기
    print_step "Step 3: 갱신 트리거 전 2초 대기 중..."
    sleep 2

    # Step 4: bus refresh 트리거
    print_step "Step 4: Config Server에서 bus refresh 트리거 중..."
    response=$(curl -s -X POST "$CONFIG_SERVER/actuator/busrefresh")
    print_success "Bus refresh가 트리거되었습니다"
    echo "$response"

    # Step 5: 전파 대기
    print_step "Step 5: 설정 전파 대기 중 (3초)..."
    sleep 3

    # Step 6: 갱신된 설정 확인
    print_step "Step 6: 모든 인스턴스의 갱신된 설정 확인 중..."
    get_config 8080
    get_config 8081
    get_config 8082

    print_success "설정 갱신 테스트가 완료되었습니다!"
}

###############################################################################
# 시나리오 2: 커스텀 이벤트 브로드캐스팅 테스트
###############################################################################

test_custom_events() {
    print_header "시나리오 2: 커스텀 이벤트 브로드캐스팅"

    # Step 1: 초기 로그인 히스토리 확인
    print_step "Step 1: 초기 로그인 히스토리 확인 중..."
    echo -e "${YELLOW}인스턴스 :8080${NC}"
    curl -s "$CLIENT_8080/events/logins" | jq '.loginHistory'

    # Step 2: 사용자 로그인 이벤트 발행
    print_step "Step 2: Config Server에서 UserLoginEvent 발행 중..."

    curl -s -X POST "$CONFIG_SERVER/events/logins" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "john.doe",
            "ipAddress": "192.168.1.100"
        }' | jq '.'

    print_success "UserLoginEvent가 발행되었습니다"

    # Step 3: 이벤트 전파 대기
    print_step "Step 3: 이벤트 전파 대기 중 (2초)..."
    sleep 2

    # Step 4: 모든 인스턴스의 로그인 히스토리 확인
    print_step "Step 4: 모든 인스턴스의 로그인 히스토리 확인 중..."

    echo -e "${YELLOW}인스턴스 :8080${NC}"
    curl -s "$CLIENT_8080/events/logins" | jq '.loginHistory'

    echo -e "${YELLOW}인스턴스 :8081${NC}"
    curl -s "$CLIENT_8081/events/logins" | jq '.loginHistory'

    echo -e "${YELLOW}인스턴스 :8082${NC}"
    curl -s "$CLIENT_8082/events/logins" | jq '.loginHistory'

    # Step 5: 추가 이벤트 발행
    print_step "Step 5: 또 다른 UserLoginEvent 발행 중..."

    curl -s -X POST "$CONFIG_SERVER/events/logins" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "alice",
            "ipAddress": "192.168.1.200"
        }' | jq '.'

    sleep 2

    # Step 6: 로그인 통계 확인
    print_step "Step 6: 로그인 통계 확인 중..."
    curl -s "$CLIENT_8080/events/logins/stats" | jq '.'

    print_success "커스텀 이벤트 브로드캐스팅 테스트가 완료되었습니다!"
}

###############################################################################
# 시나리오 3: 특정 인스턴스 갱신 테스트
###############################################################################

test_targeted_refresh() {
    print_header "시나리오 3: 특정 인스턴스 갱신"

    # Step 1: 설정 업데이트
    print_step "Step 1: 설정 업데이트 중 (Git 커밋 불필요)..."

    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    cat > "$CONFIG_REPO/client.yml" << EOF
# Config Server에서 관리하는 spring-configserver-client 애플리케이션 설정 파일
app:
  message: "Updated for targeted refresh - ${timestamp}"
  version: "alpha 3.0.0"
  feature:
    enabled: false
    max-retry: 1
    timeout-seconds: 10

database:
  pool:
    min-size: 2
    max-size: 10
    connection-timeout: 1000
EOF

    print_success "설정이 업데이트되었습니다"

    # Step 2: 특정 인스턴스에 대한 갱신 트리거
    print_step "Step 2: config-client 서비스의 :8080 포트 인스턴스만 갱신 트리거 중..."

    # config-client 서비스의 8080 포트 인스턴스 타겟팅
    curl -s -X POST "$CONFIG_SERVER/actuator/busrefresh/config-client:8080:**"

    sleep 3

    # Step 3: 대상 인스턴스만 업데이트되었는지 확인
    print_step "Step 3: 설정 확인 중..."

    echo -e "${YELLOW}인스턴스 :8080 (업데이트되어야 함)${NC}"
    get_config 8080

    echo -e "${YELLOW}인스턴스 :8081 (업데이트되지 않아야 함)${NC}"
    get_config 8081

    print_success "특정 인스턴스 갱신 테스트가 완료되었습니다!"
}

###############################################################################
# 시나리오 4: 전체 통합 테스트
###############################################################################

test_full_integration() {
    print_header "시나리오 4: 전체 통합 테스트"

    # 모든 시나리오 실행
    test_config_refresh
    echo ""
    test_custom_events
    echo ""
    test_targeted_refresh

    print_success "모든 통합 테스트가 완료되었습니다!"
}

###############################################################################
# 메인 스크립트
###############################################################################

main() {
    local scenario=${1:-0}

    echo ""
    print_header "Spring Cloud Bus 테스트 시나리오"
    echo ""

    if [ "$scenario" -eq 0 ]; then
        echo "사용 가능한 테스트 시나리오:"
        echo "  1 - Bus를 통한 설정 갱신 테스트"
        echo "  2 - 커스텀 이벤트 브로드캐스팅 테스트"
        echo "  3 - 특정 인스턴스 갱신 테스트"
        echo "  4 - 전체 통합 테스트"
        echo ""
        read -p "시나리오 선택 (1-4): " scenario
    fi

    # 서비스 실행 확인
    print_step "필요한 서비스 확인 중..."
    wait_for_service "$CONFIG_SERVER" "Config Server" || exit 1
    wait_for_service "$CLIENT_8080" "Client :8080" || exit 1
    wait_for_service "$CLIENT_8081" "Client :8081" || exit 1
    wait_for_service "$CLIENT_8082" "Client :8082" || exit 1

    echo ""

    case $scenario in
        1)
            test_config_refresh
            ;;
        2)
            test_custom_events
            ;;
        3)
            test_targeted_refresh
            ;;
        4)
            test_full_integration
            ;;
        *)
            print_error "잘못된 시나리오 번호입니다"
            exit 1
            ;;
    esac

    echo ""
    print_success "테스트 시나리오가 성공적으로 완료되었습니다!"
}

# 메인 함수 실행
main "$@"
