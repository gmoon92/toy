package com.gmoon.springquartzcluster.quartz;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springquartzcluster.test.BaseDataJpaTest;
import com.gmoon.springquartzcluster.util.LocalIpAddressUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class QuartzSchedulerHistoryRepositoryTest extends BaseDataJpaTest {
	final QuartzSchedulerHistoryRepository repository;

	@Test
	@DisplayName("스케쥴러 히스토리 저장시 로컬 ip 주소도 같이 저장한다.")
	void testSave() {
		// given
		String instanceId = "my-scheduler";
		List<String> ipAddresses = LocalIpAddressUtil.getIpAddresses();
		QuartzSchedulerHistory history = QuartzSchedulerHistory.from(instanceId, ipAddresses);

		// when
		history = repository.save(history);

		// then
		assertThat(history.getDetails())
			.isNotEmpty();
	}
}
