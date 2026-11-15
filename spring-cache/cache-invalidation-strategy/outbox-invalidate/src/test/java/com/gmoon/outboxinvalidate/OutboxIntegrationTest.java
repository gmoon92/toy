package com.gmoon.outboxinvalidate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.gmoon.cacheinvalidation.test.IntegrationTest;

/**
 * 장애를 흉내 낼 빈을 모든 테스트가 같은 목록으로 감싼다.
 * 목록이 다르면 컨텍스트가 따로 떠서 큐 소비자가 둘이 되고, 메시지가 장애를 흉내 내지 않은 쪽으로 갈 수 있다.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@IntegrationTest
@Import(RabbitContainerConfig.class)
@MockitoSpyBean(types = {CacheManager.class, RabbitTemplate.class})
public @interface OutboxIntegrationTest {
}
