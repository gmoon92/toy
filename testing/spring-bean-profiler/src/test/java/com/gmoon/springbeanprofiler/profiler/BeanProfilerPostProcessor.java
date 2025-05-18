package com.gmoon.springbeanprofiler.profiler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class BeanProfilerPostProcessor implements BeanPostProcessor, Ordered {
	private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
	private final Duration threshold;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		startTimes.put(beanName, System.currentTimeMillis());
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Long start = startTimes.remove(beanName);
		if (start != null) {
			long elapsed = System.currentTimeMillis() - start;
			if (Duration.ofMillis(elapsed).compareTo(threshold) > 0) {
				log.warn("[BEAN PROFILER] {}: {}ms", beanName, String.format("%,d", elapsed));
			}
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
