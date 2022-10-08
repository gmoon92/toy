package com.gmoon.springeventlistener.context;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Handling context refreshed event. ");
		ApplicationContext context = event.getApplicationContext();

		loggingForEventListenerBeans(context);
	}

	private void loggingForEventListenerBeans(ApplicationContext context) {
		log.info("==============ApplicationListener=====================");
		Map<String, ApplicationListener> listeners = context.getBeansOfType(ApplicationListener.class);
		for (String beanName : listeners.keySet()) {
			ApplicationListener listener = listeners.get(beanName);
			log.info("bean: {}, name: {}", listener.getClass().getSimpleName(), beanName);
		}
		log.info("======================================================");
	}
}
