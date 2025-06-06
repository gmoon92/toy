package com.gmoon.springbeanprofiler.config;

import com.gmoon.springbeanprofiler.profiler.BeanProfilerPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * Bean Profiler - 테스트 환경(@Profile("test"))에서만 활성화.
 * 모든 싱글톤 빈의 초기화 시간(ms) 콘솔 출력.
 */
@Profile("test")
@Configuration
public class BeanProfileConfig {

	@Configuration
	static class ProfilerBeanPostProcessorConfiguration {
		@Bean
		public static BeanPostProcessor beanProfilerPostProcessor(@Value("${service.test.bean.profiler.threshold:0ms}") Duration threshold) {
			return new BeanProfilerPostProcessor(threshold);
		}
	}
}
