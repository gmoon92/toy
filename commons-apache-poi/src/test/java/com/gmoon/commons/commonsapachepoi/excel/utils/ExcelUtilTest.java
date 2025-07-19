package com.gmoon.commons.commonsapachepoi.excel.utils;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;
import com.gmoon.commons.commonsapachepoi.test.TestUtils;
import com.gmoon.commons.commonsapachepoi.users.domain.Role;
import com.gmoon.commons.commonsapachepoi.users.domain.User;
import com.gmoon.commons.commonsapachepoi.users.model.ExcelUserVO;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class ExcelUtilTest {

	@Autowired
	private HttpServletRequest request;

	private final String excelFilePath = "src/test/resources/sample/test.xlsx";

	@AfterEach
	void cleanupTestFiles() throws IOException {
		Files.deleteIfExists(Paths.get(excelFilePath));
	}

	@Test
	void write() {
		List<ExcelUserVO> excelUsers = FixtureMonkey.builder()
			 .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 .build().giveMeBuilder(ExcelUserVO.class)
			 .setLazy("username", () -> TestUtils.randomString("user", 10))
			 .set("password", "111111")
			 .set("userFullname", TestUtils.randomString(10))
			 .set("role", Role.USER)
			 .setLazy("enabled", TestUtils::randomBoolean)
			 .setNull("email")
			 .sampleList(100);

		Assertions.assertThatCode(() -> write(excelFilePath, excelUsers))
			 .doesNotThrowAnyException();
	}

	private void write(String excelFilePath, List<ExcelUserVO> excelUsers) throws IOException {
		try (OutputStream outputStream = Files.newOutputStream(Paths.get(excelFilePath))) {
			ExcelUtil.write(request, outputStream, ExcelUserVO.class, excelUsers);
		}
	}

	@Test
	void read() throws IOException {
		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		int size = 10;
		List<ExcelUserVO> excelUsers = FixtureMonkey.builder()
			 .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 .build().giveMeBuilder(ExcelUserVO.class)
			 .setLazy("username", () -> TestUtils.randomString("user", 10))
			 .set("password", "111111")
			 .set("userFullname", TestUtils.randomString(10))
			 .set("role", Role.USER)
			 .setLazy("enabled", TestUtils::randomBoolean)
			 .setNull("email")
			 .sampleList(size);
		write(excelFilePath, excelUsers);

		ExcelSheet<ExcelUserVO> excelSheet = ExcelUtil.read(request, excelFilePath, ExcelUserVO.class);
		assertThat(excelSheet.isValidSheet()).isTrue();
		assertThat(excelSheet.getRows())
			 .isNotEmpty()
			 .hasSize(size)
			 .allSatisfy(
				  vo -> {
					  assertThat(vo.getUsername())
						   .as("username: 4~24자의 영문/숫자 조합이어야 한다. value: %S", vo.getUsername())
						   .matches("^[0-9a-zA-Z]{4,24}$");

					  assertThat(vo.getPassword())
						   .as("password: 엑셀 샘플의 비밀번호는 항상 '111111'이어야 한다. value: %S", vo.getPassword())
						   .isEqualTo("111111");

					  assertThat(vo.getRole())
						   .as("role: 사용자 역할은 ADMIN이 되면 안된다. value: %S", vo.getRole())
						   .isNotEqualTo(Role.ADMIN);

					  assertThat(vo.getUserFullname())
						   .as("userFullname: 사용자 이름은 비어 있으면 안된다.")
						   .isNotNull();

					  assertThat(vo.getEmail())
						   .as("email: 전체 사용자는 이메일 입력이 없어야 한다. value: %S", vo.getEmail())
						   .isBlank();
				  }
			 );
	}

	@Test
	void readSAX() throws Exception {
		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		int dataSize = 1;
		// int dataSize = 100;
		// int dataSize = 1_000;
		// int dataSize = 10_000;

		ExcelSheet<ExcelUserVO> parse = ExcelUtil.readSAX(
			 request,
			 Files.newInputStream(
				  Paths.get("src/test/resources/sample/sample-" + dataSize + ".xlsx")
			 ),
			 ExcelUserVO.class
		);

		assertThat(parse.getRows())
			 .hasSize(dataSize);
	}
}
