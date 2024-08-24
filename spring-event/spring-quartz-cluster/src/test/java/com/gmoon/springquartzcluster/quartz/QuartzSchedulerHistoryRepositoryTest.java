package com.gmoon.springquartzcluster.quartz;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springquartzcluster.test.BaseDataJpaTest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class QuartzSchedulerHistoryRepositoryTest extends BaseDataJpaTest {

	private final QuartzSchedulerHistoryRepository repository;

	@DisplayName("스케쥴러 히스토리 저장시 로컬 ip 주소도 같이 저장한다.")
	@Test
	void save() {
		String instanceId = "my-scheduler";
		List<String> ipAddresses = List.of("127.0.0.1");

		QuartzSchedulerHistory history = QuartzSchedulerHistory.from(instanceId, ipAddresses);

		assertThat(repository.saveAndFlush(history).getDetails()).isNotEmpty();
	}
}
