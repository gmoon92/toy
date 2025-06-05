package com.gmoon.core.config;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.gmoon.core.utils.SystemProperties;
import com.gmoon.javacore.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class PropertiesConfigTest {

	//EncryptablePropertySourceConverter
	@DisplayName("jasypt 프로퍼티 암호화")
	@Nested
	class JasyptConfigTest {

		@Autowired
		private Environment env;

		@Autowired
		private StringEncryptor stringEncryptor;

		@Test
		void decryptToEncryptedString() {
			String property = env.getProperty("module-name");

			String encrypt = stringEncryptor.encrypt(property);

			assertThat(stringEncryptor.decrypt(encrypt))
				 .isEqualTo(property);
		}
	}

	@DisplayName("runtime SystemProperties")
	@Nested
	class RuntimePropertyConfigTest {

		@Autowired
		private SystemProperties systemProperties;

		@DisplayName("runtime.properties 값 변경시, 변경된 값 적용")
		@Test
		@Disabled("Test 시점이 맞지 않아 disabled")
		void applyRuntimeProperties() throws InterruptedException {
			String path = "runtime.properties";
			File runtimeProperties = FileUtils.getResourceFile(path);

			String origin = FileUtils.convertFileToString(runtimeProperties);

			// change runtime property value
			FileUtils.write(runtimeProperties, origin + "module-name=core-new");
			Thread.sleep(500);

			String moduleName = systemProperties.getModuleName();
			log.info("moduleName: {}", moduleName);
			assertThat(moduleName).isEqualTo("core-new");
			FileUtils.write(runtimeProperties, origin);
		}
	}
}
