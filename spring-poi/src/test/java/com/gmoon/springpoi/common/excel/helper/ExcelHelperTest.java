package com.gmoon.springpoi.common.excel.helper;

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

import com.gmoon.springpoi.common.excel.exception.SaxReadRangeOverflowException;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;
import com.gmoon.springpoi.common.utils.TsidUtil;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
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
		 // 100, 500, 1_000,
		 // 1_500, 2_000, 2_500, 3_000,
		 // 3_500, 4_000, 4_500, 5_000,
		 // 5_500, 6_000, 6_500, 7_000,
		 // 7_500, 8_000, 8_500, 9_000,
		 // 9_500, 10_000, 10_500, 11_000,
		 // 12_000, 13_000, 14_000, 15_000,
		 // 16_000, 17_000, 18_000, 19_000,
		 // 20_000, 30_000, 40_000, 50_000,
		 // 100_000, 150_000
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

	private void write(List<ExcelUserVO> excelUsers, Path filePath) {
		try (OutputStream outputStream = Files.newOutputStream(filePath)) {
			helper.write(outputStream, ExcelUserVO.class, excelUsers);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@MeasureHeapAllocation
	@Test
	void read() {
		int size = 100;
		Path filePath = getFilePath("excel-user-" + size + ".xlsx");

		ExcelSheet<ExcelUserVO> excelSheet = helper.read(
			 filePath,
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
	}

	@ParameterizedTest
	@ValueSource(ints = {
		 1, 100, 500, 1_000,
		 // 1_500, 2_000, 2_500, 3_000,
		 // 3_500, 4_000, 4_500, 5_000
	})
	void readSAX(int dataSize) {
		String filename = String.format("excel-user-%d.xlsx", dataSize);

		ExcelSheet<ExcelUserVO> parse = helper.readSAX(
			 getFilePath(filename),
			 ExcelUserVO.class,
			 dataSize
		);

		assertThat(parse.isValidSheet()).isTrue();
		assertThat(parse.size()).isEqualTo(dataSize);
	}

	@Test
	void invalid() {
		int size = 100;
		String filename = "invalid-" + size + ".xlsx";

		ExcelSheet<ExcelUserVO> parse = helper.readSAX(
			 getFilePath(filename),
			 ExcelUserVO.class,
			 size
		);

		assertThat(parse.size()).isEqualTo(size);
		assertThat(parse.isValidSheet()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = {
		 1, 100, 500, 1_000,
		 // 1_500, 2_000, 2_500, 3_000,
		 // 3_500, 4_000, 4_500, 5_000
	})
	void getDataRows(int dataSize) {
		String filename = "excel-user-" + dataSize + ".xlsx";

		assertThat(getRows(filename, dataSize)).isEqualTo(dataSize);
		assertThat(getRows(filename, dataSize + 1)).isEqualTo(dataSize);
		assertThatThrownBy(() -> getRows(filename, dataSize - 1))
			 .isInstanceOf(SaxReadRangeOverflowException.class);
	}

	private long getRows(String filename, int dataSize) {
		return helper.getDataRows(
			 getFilePath(filename),
			 ExcelSheetType.USER,
			 dataSize
		);
	}
}
