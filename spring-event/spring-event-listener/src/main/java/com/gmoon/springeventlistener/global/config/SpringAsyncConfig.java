package com.gmoon.springeventlistener.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync(mode = AdviceMode.PROXY, proxyTargetClass = true)
@Configuration
public class SpringAsyncConfig implements AsyncConfigurer {

	private static final String THREAD_NAME_PREFIX = "thread-gmoon-pool";
	private static final int DEFAULT_CORE_SIZE = 16;
	private static final int MAX_CORE_SIZE = 32;
	private static final int POOL_KEEP_ALIVE_SECONDS = 120;
	private static final int POOL_QUEUE_CAPACITY = 500;

	@Bean("taskExecutor")
	@Primary
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);
		taskExecutor.setCorePoolSize(DEFAULT_CORE_SIZE);
		taskExecutor.setMaxPoolSize(MAX_CORE_SIZE);
		taskExecutor.setKeepAliveSeconds(POOL_KEEP_ALIVE_SECONDS);
		taskExecutor.setQueueCapacity(POOL_QUEUE_CAPACITY);
		taskExecutor.setAllowCoreThreadTimeOut(false);
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		taskExecutor.initialize();
		return taskExecutor;
	}
}
