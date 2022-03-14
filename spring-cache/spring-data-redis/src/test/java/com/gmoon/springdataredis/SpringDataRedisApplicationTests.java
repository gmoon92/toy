package com.gmoon.springdataredis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.gmoon.springdataredis.test.EmbeddedRedisConfig;

@SpringBootTest
@ActiveProfiles(value = "local")
@Import(EmbeddedRedisConfig.class)
class SpringDataRedisApplicationTests {

	@Test
	void contextLoads() {
	}

}
