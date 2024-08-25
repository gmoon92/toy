package com.gmoon.hibernateenvers.global.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;

import com.gmoon.hibernateenvers.revision.domain.QRevision;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PageUtilsTest {

	@Test
	void test() {
		Path<?> createdAt = QRevision.revision.createdAt;
		PathMetadata metadata = createdAt.getMetadata();

		log.info("metadata: {}", metadata.getRootPath());
		log.info("metadata: {}", metadata.getName());

		assertThat(metadata.getRootPath().toString()).isEqualTo("revision");
		assertThat(metadata.getName()).isEqualTo("createdAt");
	}
}
