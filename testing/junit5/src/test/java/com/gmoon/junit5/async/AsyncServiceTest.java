package com.gmoon.junit5.async;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.junit5.async.config.SpringAsyncConfig;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
	AsyncServiceTest.SyncTestConfig.class,
	AsyncService.class
})
class AsyncServiceTest {

	@Autowired
	private AsyncService asyncService;

	@Test
	void testAsync() throws Exception {
		int result = asyncService.handle()
			.get();

		assertThat(result).isEqualTo(1);
	}

	@TestConfiguration
	static class SyncTestConfig extends SpringAsyncConfig {

		@Bean
		@Primary
		@Override
		public Executor getAsyncExecutor() {
			return new SyncTaskExecutor();
		}
	}
}
