package com.gmoon.outboxinvalidate.config;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.gmoon.outboxinvalidate.cache.EvictionRelay;

import lombok.RequiredArgsConstructor;

/**
 * 릴레이를 {@code service.cache.outbox.relay-interval} 마다 돌린다.
 * 첫 실행도 한 간격 뒤에 한다.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RelayConfig implements SchedulingConfigurer {

	private final EvictionRelay evictionRelay;
	private final ServiceCacheProperties properties;

	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		Duration interval = properties.outbox().relayInterval();
		registrar.addFixedDelayTask(new FixedDelayTask(evictionRelay::relay, interval, interval));
	}
}
