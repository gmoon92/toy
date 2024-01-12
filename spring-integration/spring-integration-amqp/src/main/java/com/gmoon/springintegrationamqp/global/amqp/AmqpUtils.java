package com.gmoon.springintegrationamqp.global.amqp;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AmqpUtils {

	private final RabbitTemplate rabbitTemplate;
	private final AmqpAdmin amqpAdmin;
	private final Client client;

	/**
	 * using MessageChannel
	 * */
	@Deprecated
	public void send(String routingKey, Serializable message) {
		rabbitTemplate.convertAndSend(routingKey, message);
	}

	private void declareQueues() {
		for (AmqpMessageDestination destination : AmqpMessageDestination.values()) {
			String queueName = destination.value;
			deleteQueue(queueName);
			declarePersistenceQueue(queueName);
		}
	}

	/**
	 * <pre>
	 * {@link Queue}
	 * - durable: MQ 서버 재시작해도 유지
	 * - exclusive: 컨슈머와 연결이 끊어진 큐 자동 삭제
	 * 			 	RabbitMQ 에 연결한 몇몇 애플리케이션 서버가 있음으로
	 * 			 	다른 연결로부터 큐에 접근할 수 있다.
	 * - autoDelete: 모든 컨슈머와 연결이 끊어진 큐 자동 삭제
	 * 				큐에서 소비할 대상이 없더라도 큐를 유지
	 * </pre>
	 *
	 * <p>https://www.rabbitmq.com/queues.html#properties
	 * <p>https://stackoverflow.com/questions/21248563/rabbitmq-difference-between-exclusive-and-auto-delete
	 *
	 * @return
	 */
	private String declarePersistenceQueue(String name) {
		try {
			Queue queue = new Queue(
				name,
				true,
				false,
				false,
				getQueueArguments()
			);
			return amqpAdmin.declareQueue(queue);
		} catch (Exception e) {
			log.warn("Not declare queue " + name, e);
		}
		return null;
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

	private void deleteGarbageQueues() {
		List<QueueInfo> queues = getQueueInfos();
		for (QueueInfo queue : queues) {
			if (queue.getConsumerCount() == 0) {
				deleteQueue(queue.getName());
			}
		}
	}

	private List<QueueInfo> getQueueInfos() {
		return client.getQueues();
	}

	private void deleteQueue(String queue) {
		amqpAdmin.deleteQueue(queue);
	}
}
