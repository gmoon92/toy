package com.gmoon.springkafkabroker.adminclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicCollection;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see <a href="https://kafka.apache.org/40/javadoc/org/apache/kafka/clients/admin/package-summary.html">org.apache,kafka.clients.admin</a>
 * @see <a href="https://docs.confluent.io/platform/current/installation/configuration/admin-configs.html">Kafka AdminClient Configurations for Confluent Platform</a>
 * @see <a href="https://kafka.apache.org/documentation/#topicconfigs">Topic configs</a>
 */
@Slf4j
@Disabled
class KafkaAdminClientTest {

	private static AdminClient adminClient;

	/**
	 * @see AdminClientConfig
	 */
	@BeforeAll
	static void beforeAll() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("request.timeout.ms", 1000);
		props.put("default.api.timeout.ms", 1000);

//		HA 설정, deprecated kafka 2.6+ 생략 가능.
//		props.put("client.dns.lookup", "use_all_dns_ips");
		adminClient = AdminClient.create(props);
	}

	@AfterAll
	static void afterAll() {
		adminClient.close();
	}

	@Test
	void test() throws Exception {
		loggingTopicAll(adminClient); // 클러스트에 존재하는 토픽 조회

		// 토픽 생성
		final var topicName = "sample.topic.01";
		NewTopic newTopic = new NewTopic(topicName, 3, (short) 1);

		CreateTopicsResult createdTopics = adminClient.createTopics(List.of(newTopic));
		log.info("create topic -> {}@{}", topicName, createdTopics.topicId(topicName).get());
		loggingTopicAll(adminClient);

		// 토픽 제거
		adminClient.deleteTopics(List.of(topicName));
		log.info("remove topic -> {}", topicName);
		loggingTopicAll(adminClient);
	}

	private void loggingTopicAll(AdminClient adminClient) throws Exception {
		log.info("topics: {}",
			 String.join(
				  ", ",
				  adminClient.listTopics()
					   .names()
					   .get()
			 )
		);
	}

	@DisplayName("토픽 조회 - map")
	@Test
	void describeTopics() throws Exception {
		DescribeTopicsResult result = adminClient.describeTopics(
			 TopicCollection.ofTopicNames(List.of("sample"))
		);

		Map<String, TopicDescription> map = result.allTopicNames().get(500, TimeUnit.MILLISECONDS);
		TopicDescription topic = map.get("sample");
		log.info("topic: {}", topic);
	}

	@DisplayName("토픽 조회 - list")
	@Test
	void listings() throws Exception {
		// 클러스트에 존재하는 토픽 조회
		ListTopicsResult result = adminClient.listTopics();

		Collection<TopicListing> topicListings = result.listings().get(500, TimeUnit.MILLISECONDS);
		for (TopicListing topic : topicListings) {
			log.info("topic id: {}, name: {}, internal: {}",
				 topic.topicId(), topic.name(), topic.isInternal());
		}
	}

	/**
	 * @apiNote Kafka {@link ConfigResource} 를 이용해 브로커/토픽 설정을 조회 및 변경할 수 있다.
	 *
	 * <pre>
	 *     주의: 동작 설정(dynamic config) 만 변경 가능. (e.g. `max.message.bytes`)
	 *          정적 설정(static config) 는 서버 재시작 필요. (e.g. `broker.id`, `log.dirs`)
	 *
	 *     설정 가능한 자원:
	 *     - {@link ConfigResource.Type#BROKER}: 브로커 전체 설정
	 *     - {@link ConfigResource.Type#BROKER_LOGGER}: 브로커 로그
	 *     - {@link ConfigResource.Type#TOPIC}: 토픽별 설정(e.g. `cleanup.policy`: 토픽의 청소 정책(예: compact, delete 등))
	 * </pre>
	 */
	@DisplayName("토픽 설정 조회 및 변경")
	@Test
	void configResource() throws Exception {
		// 조회할 토픽 지정
		String topicName = "sample";
		ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

		DescribeConfigsResult result = adminClient.describeConfigs(List.of(configResource));
		Map<ConfigResource, KafkaFuture<Config>> configMap = result.values();
		Config config = configMap.get(configResource).get();
		for (ConfigEntry entry : config.entries()) {
			// 토픽 설정 조회, 기본 값 확인
			log.info("default={}, {}={}", entry.isDefault(), entry.name(), entry.value());
		}

		// 특정 토픽 설정 확인
		log.info("특정 토픽 설정 조회=======");
		String configName = TopicConfig.CLEANUP_POLICY_CONFIG; // cleanup.policy
		ConfigEntry cleanupPolicy = config.get(configName);
		log.info("{}={}", cleanupPolicy.name(), cleanupPolicy.value());

		// compact 설정으로 변경
		String compact = TopicConfig.CLEANUP_POLICY_COMPACT; // compact
		if (!cleanupPolicy.value().equals(compact)) {
			ConfigEntry compactConfig = new ConfigEntry(configName, compact);
			alterConfigOption(configResource, compactConfig, AlterConfigOp.OpType.SET);

			ConfigEntry configEntry = getConfigEntry(configResource, compactConfig.name());
			assertThat(configEntry.value()).isEqualTo(TopicConfig.CLEANUP_POLICY_COMPACT);

		}

		// 기본 값으로 설정 되돌리기
		alterConfigOption(configResource, cleanupPolicy, AlterConfigOp.OpType.DELETE);

		ConfigEntry configEntry = getConfigEntry(configResource, configName);
		assertThat(configEntry.value()).isEqualTo(TopicConfig.CLEANUP_POLICY_DELETE);
	}

	/**
	 * @apiNote <pre>
	 *  Kafka 토픽/브로커 설정을 증분 변경(Incremental Alter)하는 메서드.
	 *
	 * @param configResource 설정 목록
	 * @param configEntry 변경할 설정 값
	 * @param operationType 설정 변경 동작 방식. 각 동작은 설정 이름과 설정값을 기준으로 수행된다.
	 *  - {@link AlterConfigOp.OpType#SET}: 설정 값을 덮어쓴다.
	 *  - {@link AlterConfigOp.OpType#DELETE}: 설정 값을 삭제하고, 기본 값으로 설정한다.
	 *  - {@link AlterConfigOp.OpType#APPEND}: 배열 값 추가 옵션, 설정 값을 추가한다.(배열 형식의 설정 값만 가능)
	 *  - {@link AlterConfigOp.OpType#SUBTRACT}: 배열 값 삭제 옵션, 지정된 설정 값 제거한다. 모든 항목을 제거되면 빈 목록 값(`""`)으로 설정되며, 기본값으로 재설정되지 않는다.(배열 형식의 설정 값만 가능)
	 * </pre>
	 * @see AlterConfigOp
	 * @see <a href="https://kafka.apache.org/documentation/#topicconfigs">Topic configs</a>
	 */
	private void alterConfigOption(
		 ConfigResource configResource,
		 ConfigEntry configEntry,
		 AlterConfigOp.OpType operationType
	) {
		Map<ConfigResource, Collection<AlterConfigOp>> alterConfigs = Map.of(
			 configResource, List.of(new AlterConfigOp(configEntry, operationType))
		);
		AlterConfigsResult result = adminClient.incrementalAlterConfigs(alterConfigs);
		try {
			result.all().get();
			log.info("[Alter {}] {}={}", operationType, configEntry.name(), configEntry.value());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("토픽 설정 변경 중 오류 발생", e);
		}
	}

	private ConfigEntry getConfigEntry(ConfigResource configResource, String configKey) {
		try {
			return adminClient.describeConfigs(List.of(configResource))
				 .values().get(configResource)
				 .get()
				 .get(configKey);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
