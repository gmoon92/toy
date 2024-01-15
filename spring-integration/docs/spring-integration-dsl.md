# Spring Integration DSL

## [IntegrationFlow DSL](https://spring.io/blog/2014/11/25/spring-integration-java-dsl-line-by-line-tutorial/)

- from
    - MessageChannel
        - channel
        - fixedSubscriberChannel
            - 인터셉터나 데이터 유형을 지원하지 않음
        - publishSubscribeChannel
        - nullChannel
- transform
- filter
- headerFilter
- wireTap
- split
- route
- handle
- controlBus
- convert
- bridge
- delay
- enrich
- enrichHeaders
- claimCheckIn
- claimCheckOut
- resequence
- aggregate
- gateway
- scatterGather
- barrier
- trigger
- intercept
- fluxTransform
- log
- logAndReply
- to
