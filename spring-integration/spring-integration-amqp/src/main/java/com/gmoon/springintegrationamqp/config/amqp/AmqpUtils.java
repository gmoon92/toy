package com.gmoon.springintegrationamqp.config.amqp;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AmqpUtils {

	private final AmqpAdmin amqpAdmin;
	private final Client client;

	/**
	 * <p>{@link Queue}
	 * <p>- durable: MQ 서버 재시작해도 유지
	 * <p>- exclusive: 컨슈머와 연결이 끊어진 큐 자동 삭제
	 * <p>- autoDelete: 모든 컨슈머와 연결이 끊어진 큐 자동 삭제
	 *
	 * <p>https://www.rabbitmq.com/queues.html#properties
	 */
	public Queue declarePersistenceQueue(String name) {
		Queue queue = new Queue(
			name,
			true,
			false,
			false,
			getQueueArguments()
		);

		amqpAdmin.declareQueue(queue);
		return queue;
	}

	/**
	 * <p>RabbitMQ Queue 속성 참고
	 * <p>https://www.rabbitmq.com/parameters.html#policies
	 */
	private Map<String, Object> getQueueArguments() {
		Map<String, Object> args = new HashMap<>();
		args.put("x-expires", Duration.ofHours(1).toMillis());
		args.put("x-message-ttl", Duration.ofHours(1).toMillis());
		args.put("x-ha-mode", "all");
		return args;
	}

	private void declareQueues() {
		for (AmqpMessageDestination destination : AmqpMessageDestination.values()) {
			String queueName = destination.value;
			deleteQueue(queueName);
			declarePersistenceQueue(queueName);
		}
	}

	private void deleteGarbageQueues() {
		List<QueueInfo> queues = client.getQueues();
		for (QueueInfo queue : queues) {
			if (queue.getConsumerCount() == 0) {
				deleteQueue(queue.getName());
			}
		}
	}

	private void deleteQueue(String queueName) {
		amqpAdmin.deleteQueue(queueName);
	}
}
