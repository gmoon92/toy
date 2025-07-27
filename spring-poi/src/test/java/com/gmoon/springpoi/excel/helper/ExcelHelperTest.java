package com.gmoon.springpoi.excel.helper;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import com.gmoon.springpoi.common.utils.TsidUtil;
import com.gmoon.springpoi.excel.vo.ExcelSheet;
import com.gmoon.springpoi.test.TestUtils;
import com.gmoon.springpoi.users.domain.Role;
import com.gmoon.springpoi.users.domain.User;
import com.gmoon.springpoi.users.model.ExcelUserVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuickPerfTest
@SpringBootTest
class ExcelHelperTest {

	@Autowired
	private ExcelHelper helper;

	@BeforeEach
	void setUp() {
		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}

	private Path getFilePath(String filename) {
		return Paths.get("src/test/resources/sample/", filename);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		 1,
		 100
		 // 100,
		 // 500,
		 // 1_000,
		 // 5_000,
		 // 10_000,
		 // 15_000,
		 // 100_000,
		 // 150_000
	})
	void write(int size) throws IOException {
		String filename = "benchmark-" + size + ".xlsx";
		Path filePath = getFilePath(filename);

		List<ExcelUserVO> excelUsers = FixtureMonkey.builder()
			 .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 .build().giveMeBuilder(ExcelUserVO.class)
			 .setLazy("username", () -> "user" + TsidUtil.generate())
			 .set("password", "111111")
			 .set("userFullname", TestUtils.randomString(10))
			 .setLazy("gender", () -> TestUtils.randomInteger(0, 1))
			 .setLazy("role", () -> TestUtils.pickRandom(Role.USER, Role.MANAGER))
			 .setLazy("enabled", TestUtils::randomBoolean)
			 .setNull("email")
			 .sampleList(size);

		Assertions.assertThatCode(() -> write(excelUsers, filePath))
			 .doesNotThrowAnyException();

		Files.deleteIfExists(filePath);
	}

	private void write(List<ExcelUserVO> excelUsers, Path filePath) throws IOException {
		try (OutputStream outputStream = Files.newOutputStream(filePath)) {
			helper.write(outputStream, ExcelUserVO.class, excelUsers);
		}
	}

	@MeasureHeapAllocation
	@Test
	void read() throws IOException {
		Path filePath = getFilePath("excel-user.xlsx");

		int size = 10;
		List<ExcelUserVO> excelUsers = FixtureMonkey.builder()
			 .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 .build().giveMeBuilder(ExcelUserVO.class)
			 .setLazy("username", () -> TestUtils.randomString("user", 10))
			 .set("password", "111111")
			 .set("userFullname", TestUtils.randomString(10))
			 .setLazy("gender", () -> TestUtils.randomInteger(0, 1))
			 .setLazy("role", () -> TestUtils.pickRandom(Role.USER, Role.MANAGER))
			 .setLazy("enabled", TestUtils::randomBoolean)
			 .setNull("email")
			 .sampleList(size);
		write(excelUsers, filePath);

		ExcelSheet<ExcelUserVO> excelSheet = helper.read(
			 filePath.toString(),
			 ExcelUserVO.class
		);
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

		Files.deleteIfExists(filePath);
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 100, 1_000})
	void readSAX(int dataSize) throws Exception {
		String filename = String.format("sample-%d.xlsx", dataSize);
		Path filePath = getFilePath(filename);

		ExcelSheet<ExcelUserVO> parse = helper.readSAX(
			 Files.newInputStream(filePath),
			 ExcelUserVO.class
		);

		assertThat(parse.isValidSheet()).isTrue();
		assertThat(parse.size()).isEqualTo(dataSize);
	}

	@Test
	void invalid() throws IOException {
		int size = 100;
		String filename = "invalid-" + size + ".xlsx";

		ExcelSheet<ExcelUserVO> parse = helper.readSAX(
			 Files.newInputStream(Paths.get("src/test/resources/sample/", filename)),
			 ExcelUserVO.class
		);

		assertThat(parse.size()).isEqualTo(size);
		assertThat(parse.isValidSheet()).isFalse();
	}
}
