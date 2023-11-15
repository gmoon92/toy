package com.gmoon.batchinsert.global;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.batchinsert.global.prop.StorageProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class StoragePropertiesTest {

	@Autowired
	private StorageProperties properties;

	@Test
	void read() {
		assertThat(properties.getPath()).isNotBlank();
	}
}
