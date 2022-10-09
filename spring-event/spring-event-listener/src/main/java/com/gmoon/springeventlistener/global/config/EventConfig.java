package com.gmoon.springeventlistener.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class EventConfig {

	@Bean(name = "applicationEventMulticaster")
	public ApplicationEventMulticaster simpleApplicationEventMulticaster(Executor executor) {
		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

		log.info("executor: {}", executor);
		eventMulticaster.setTaskExecutor(executor);
		return eventMulticaster;
	}

	@Bean
	@ConditionalOnProperty(value = "async.task", havingValue = "pool", matchIfMissing = true)
	public Executor asyncTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setThreadNamePrefix("thread-gmoon-pool");
		taskExecutor.setCorePoolSize(16);
		taskExecutor.setMaxPoolSize(32);
		taskExecutor.setKeepAliveSeconds(120);
		taskExecutor.setQueueCapacity(500);
		taskExecutor.setAllowCoreThreadTimeOut(false);
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		taskExecutor.initialize();
		return taskExecutor;
	}

	@Bean
	@ConditionalOnMissingBean(Executor.class)
	public Executor simpleAsyncTaskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}
}
