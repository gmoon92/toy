# Kafka Consumer Offset Commit

Kafka에서 오프셋 커밋은 "어디까지 읽었는지"를 저장하는 과정이며, 이를 통해 중복 처리/메시지 유실을 방지할 수 있다.

## Kafka에서 오프셋(Offset)과 커밋(Commit)이란?

Kafka에서 오프셋(offset)과 커밋(commit) 개념은 메시지를 정확히 한 번씩 처리하거나, 중복/유실 없이 메시지를 소비(consume)하는 데 중요한 개념이다. 우선, 이 개념을 차근차근 알아보자.

## 1. Kafka에서 오프셋(Offset)이란?

Kafka에서 오프셋(offset)은 각 메시지가 저장된 위치(번호) 를 의미한다.

Kafka의 메시지 저장 구조
- 메시지는 특정 토픽(topic) 안에 있는 파티션(partition) 에 저장된다.
- Kafka는 데이터를 FIFO(First-In-First-Out) 방식으로 처리하며, 각 메시지에는 고유한 숫자(=오프셋) 가 부여된다.
- 컨슈머가 메시지를 읽을 때, 어디까지 읽었는지 오프셋을 기억해야 중복 소비를 방지할 수 있다.

예를 들어, Kafka에 다음과 같은 메시지가 있다고 가정한다.

| Partition 0           |
|-----------------------|
| Offset 0 → "Hello"    |
| Offset 1 → "Kafka"    |
| Offset 2 → "Consumer" |
| Offset 3 → "Offset"   |

- 컨슈머가 메시지를 읽을 때 "어디까지 읽었는지(=현재 오프셋)" 를 알고 있어야 한다.
- 오프셋을 저장하지 않으면, 다시 시작할 때 처음부터 읽게 된다. → 중복 처리 발생 가능성

## 2. Kafka에서 커밋(Commit)이란?

Kafka에서 커밋(commit)은 "내가 이 오프셋까지 읽었다!"라고 저장하는 과정이다.

Kafka 컨슈머는 메시지를 가져오면, Kafka에게 "나 여기까지 읽었다." 라고 오프셋을 저장(커밋)할 수 있다. 이렇게 하면, 컨슈머가 다시 실행되거나 리밸런스가 발생해도 저장된 오프셋 이후의 메시지부터 읽을 수 있다.

커밋을 하지 않으면?
- 컨슈머가 장애로 종료되었다가 다시 시작되면 처음부터 다시 읽는다.(중복 소비 발생 가능)
- 컨슈머 그룹이 리밸런스되면 다른 컨슈머가 처음부터 다시 읽을 수도 있다.

커밋을 하면?
- 컨슈머가 재시작되어도 마지막으로 커밋된 오프셋 이후부터 읽는다.
- 리밸런스가 발생해도 중복 처리 없이 이어서 읽을 수 있다.

## 3. Kafka의 오프셋 커밋 방법

Kafka에서는 오프셋을 커밋하는 방법이 여러 가지가 있다.

### 3.1 자동 커밋 (enable.auto.commit=true)

Kafka가 자동으로 오프셋을 저장하는 방식이다.

```properties
enable.auto.commit=true
auto.commit.interval.ms=5000  # 5초마다 자동 커밋
```
- 장점: 설정만 하면 알아서 동작 → 사용하기 간편
- 단점:
    - 메시지를 처리하기 전에 오프셋이 자동으로 저장될 수도 있음 → 메시지 유실 가능성
    - 리밸런스가 발생하면 일부 메시지를 중복 소비할 수도 있음.

### 3.2. 동기 커밋 (commitSync)

```java
// 현재까지 읽은 오프셋을 동기적으로 커밋
consumer.commitSync();
```

- 컨슈머가 직접 현재까지 처리한 오프셋을 커밋하는 방식이다.
- `commitSync()` 메서드를 호출하면, Kafka 브로커와 통신하여 오프셋을 저장한다.
- 장점: 오프셋이 반드시 저장됨 → 메시지 유실 방지.
- 단점: Kafka와 네트워크 통신을 해야 하기 때문에 속도가 느릴 수 있음.

### 3.3. 비동기 커밋 (commitAsync)

```java
consumer.commitAsync((offsets, exception) -> {
  if (exception != null) {
      log.error("커밋 실패: {}", exception.getMessage());
  }
});
```

- `commitAsync()`는 오프셋을 비동기적으로 저장하는 방식이다.
- 장점: Kafka와 비동기 통신하여 성능이 빠름
- 단점: 커밋이 실패해도 보장되지 않음! → 재시도 로직이 필요함.

## 4. Kafka Consumer 예제 코드

```java
@Test
void asyncCallback() {
	try (Consumer<String, String> consumer = new KafkaConsumer<>(getProperties())) {
		consumer.subscribe(List.of(topic));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

			for (ConsumerRecord<String, String> record : records) {
				log.info("Kafka Message Consumed - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}, Timestamp: {}",
				  record.topic(), record.partition(), record.offset(), record.key(), record.value(), record.timestamp());

				// 비동기 커밋
				consumer.commitAsync(
				  // callback.
				  (offsets, exception) -> {
					  if (exception != null) {
						  log.error("Async Commit Failed for Offsets: {} - Exception: {}", offsets, exception.getMessage());
					  }
				  });
			}
		}
	} catch (Exception e) {
		log.error("Kafka Consumer Error: {}", e.getMessage());
	}
}
```

## 5. Kafka에서 커밋이 왜 중요할까?

오프셋을 커밋하지 않으면?
- 컨슈머가 중단되었다가 다시 실행되면 처음부터 다시 읽는다.
- 리밸런스가 발생하면 다른 컨슈머가 같은 메시지를 다시 소비할 수도 있다. → 중복 소비 발생

오프셋을 커밋하면?
- 컨슈머가 재시작될 때 마지막으로 커밋한 오프셋 이후부터 읽을 수 있다.
- 리밸런스 발생해도 중복 처리를 최소화할 수 있다.

커밋 전략을 잘 선택하면, 중복 처리와 성능을 적절히 조절할 수 있다.
1. 빠르고 단순한 처리가 중요하면? → `commitAsync()`
2. 메시지 유실 없이 정확한 처리가 중요하면? → `commitSync()`
3. 최적의 방식은? → `commitAsync()`로 주기적으로 저장하고, 종료 시 `commitSync()` 사용.
