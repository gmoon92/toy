package com.gmoon.commons.commonsapachepoi.excel.utils;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelModel;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.converter.common.StringYNToBooleanConverter;
import com.gmoon.commons.commonsapachepoi.excel.converter.users.StringToRoleConverter;
import com.gmoon.commons.commonsapachepoi.excel.validator.common.BooleanStringYNValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.common.LineBreakNotAllowedValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.common.UniqueCellValueValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.users.UserEmailValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.users.UserRoleValidator;
import com.gmoon.commons.commonsapachepoi.excel.validator.users.UsernameValidator;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;
import com.gmoon.commons.commonsapachepoi.users.Role;
import com.gmoon.commons.commonsapachepoi.users.User;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.ToString;
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
	void download() {
		Assertions.assertThatCode(() -> download(100, excelFilePath))
			 .doesNotThrowAnyException();
	}

	private void download(int size, String excelFilePath) throws IOException {
		RandomUtils randomUtils = RandomUtils.secure();
		RandomStringUtils randomStringUtils = RandomStringUtils.secure();
		List<ExcelUserVO> excelUsers = FixtureMonkey.builder()
			 .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 .build().giveMeBuilder(ExcelUserVO.class)
			 .setLazy("username", () -> "user" + randomStringUtils.nextAlphanumeric(10))
			 .set("password", "111111")
			 .set("userFullname", randomStringUtils.nextAlphanumeric(10))
			 .set("role", Role.USER)
			 .setLazy("enabled", randomUtils::randomBoolean)
			 .setNull("email")
			 .sampleList(size);

		try (OutputStream outputStream = Files.newOutputStream(Paths.get(excelFilePath))) {
			ExcelUtil.download(request, outputStream, ExcelUserVO.class, excelUsers);
		}
	}

	@Test
	void upload() throws IOException {
		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		int size = 10;
		download(size, excelFilePath);

		ExcelSheet<ExcelUserVO> excelSheet = ExcelUtil.read(request, excelFilePath, ExcelUserVO.class);
		assertThat(excelSheet.isValidSheet()).isTrue();
		assertThat(excelSheet.getRows()).isNotEmpty()
			 .hasSize(size)
			 .allSatisfy(
				  vo -> {
					  assertThat(vo.getUsername())
						   .matches("^[0-9a-zA-Z]{4,24}$")
						   .as("username: 4~24자의 영문/숫자 조합이어야 한다. value: %S", vo.getUsername());

					  assertThat(vo.getPassword())
						   .isEqualTo("111111")
						   .as("password: 엑셀 샘플의 비밀번호는 항상 '111111'이어야 한다. value: %S", vo.getPassword());

					  assertThat(vo.getRole())
						   .isNotEqualTo(Role.ADMIN)
						   .as("role: 사용자 역할은 ADMIN이 되면 안된다. value: %S", vo.getRole());

					  assertThat(vo.getUserFullname())
						   .isNotNull()
						   .as("userFullname: 사용자 이름은 비어 있으면 안된다.");

					  assertThat(vo.getEmail())
						   .as("email: 전체 사용자는 이메일 입력이 없어야 한다. value: %S", vo.getEmail())
						   .isEmpty();
				  }
			 );
	}

	@ExcelModel
	@Getter
	@ToString
	public static class ExcelUserVO {

		@ExcelProperty(
			 title = "사용자 아이디",
			 comment = "사용자 아이디는 필수 입력 값입니다.",
			 required = true,
			 validators = {
				  LineBreakNotAllowedValidator.class,
				  UniqueCellValueValidator.class,
				  UsernameValidator.class
			 }
		)
		private String username;

		@ExcelProperty(title = "사용자 비밀번호", required = true)
		private String password;

		@ExcelProperty(title = "사용자 이름", required = true)
		private String userFullname;

		@ExcelProperty(
			 title = "사용자 역할",
			 comment = "사용자 역할은 'MANAGER', 'USER' 만 가능합니다.",
			 required = true,
			 validators = UserRoleValidator.class,
			 converter = StringToRoleConverter.class
		)
		private Role role;

		@ExcelProperty(title = "이메일", validators = UserEmailValidator.class)
		private String email;

		@ExcelProperty(
			 title = "활성화",
			 comment = "활성화는 'Y'/'N' 입력만 가능합니다.",
			 required = true,
			 validators = BooleanStringYNValidator.class,
			 converter = StringYNToBooleanConverter.class
		)
		private boolean enabled;
	}
}
