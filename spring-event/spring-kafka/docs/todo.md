# Kafka todo

우리가 다뤄야할 점
- 처리량
    - 메시지 압축
    - asks
- 메시지 배달 사고 방지
    - 멱등성 프로듀서
    - 멱등성 컨슈머
- 소스 구현 방향성
    - [Spring kafka - `@KafkaListener`](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html)
    - [Spring Integration](https://docs.spring.io/spring-integration/reference/kafka.html)

```text
    Producer           kafka         Consumer
Source Application --> broker <-- Target Application
```

- Source Application: 데이터 전송
- Target Application: 데이터 처리

- Broker
    - 토픽의 파티션의 메시지 저장 주기
    - 토픽이란
        - 파티션 별로 관리 세그먼트로 관리한다.
        - 토픽 이름 정하기
    - 파티션 할당, 인덱스, 보존 정책, 로그 압착
    - Admin Client
        - 토픽 생성하기
- Producer
    - 멱등성 프로듀서, 중복 없는 처리
    - 메시지 전송
    - 신뢰성 있는 데이터 전달, 복제
        - 배치 전송
        - 중복 없는 전송
        - 정확히 한 번 전송
        - 재시도 처리
        - `delivery.timeout.ms`
        - `enable.idempotence`
- Consumer
    - Consumer Group 토픽 파티션 맵핑
        - 애플리케이션 목적별 그룹별 셋팅
            - group a -> elasticsearch 적재 consumer (로그 실시간 확인용)
            - group b -> hadoop 적재 consumer (대용량 데이터 적재 또는 데이터 통합)
    - 메시지 처리
        - 중복 없는 처리
        - 배치 처리
    - 개념
        - 컨슈머 그룹
        - 리밸런스
        - 컨슈머 생성
        - 토픽 구독
        - 폴링 루프
        - 컨슈머 설정
        - 파티션 할당 전략
    - 실전
        - 폴링 루프 벗어나기
        - 디시리얼라이저
        - 컨슈머 그룹없이 컨슈머 사용
- Connect + Transaction outbox pattern
- 운영
    - Lag 랙을 통해 지연되는 처리량 파악
    - 파티션 관리
    - 토픽 작업, 컨슈머 그룹 작업, 동적 설정 변경
    - 암호화, 인가, 감사
