package com.gmoon.cacheinvalidation.core.testsupport;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 데이터소스 출처만 프로파일로 갈아끼운다.
 * <ul>
 *     <li>기본: {@code application-test.yml} 의 공유 DB 서버 + 모듈별 스키마 (다른 모듈과 동일)</li>
 *     <li>{@code -Ptestcontainers}: 컨테이너를 띄워 접속 정보를 덮어씀</li>
 * </ul>
 * 스키마명, ddl-auto, data.sql 은 두 경로에서 동일하다.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@Import(CacheContainerConfig.class)
@ActiveProfiles(resolver = CacheTestProfileResolver.class)
public @interface CacheIntegrationTest {
}
