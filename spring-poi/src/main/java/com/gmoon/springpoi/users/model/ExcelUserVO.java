package com.gmoon.springpoi.users.model;

import com.gmoon.springpoi.excel.annotation.ExcelModel;
import com.gmoon.springpoi.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.excel.converter.common.StringToIntegerConverter;
import com.gmoon.springpoi.excel.converter.common.StringYNToBooleanConverter;
import com.gmoon.springpoi.excel.converter.users.StringToRoleConverter;
import com.gmoon.springpoi.excel.validator.common.BooleanStringYNValidator;
import com.gmoon.springpoi.excel.validator.common.LineBreakNotAllowedValidator;
import com.gmoon.springpoi.excel.validator.common.UniqueCellValueValidator;
import com.gmoon.springpoi.excel.validator.users.UseGenderValidator;
import com.gmoon.springpoi.excel.validator.users.UserEmailValidator;
import com.gmoon.springpoi.excel.validator.users.UserRoleValidator;
import com.gmoon.springpoi.excel.validator.users.UsernameValidator;
import com.gmoon.springpoi.users.domain.Role;

import lombok.Getter;
import lombok.ToString;

@ExcelModel
@Getter
@ToString
public class ExcelUserVO {

	@ExcelProperty(
		 title = "사용자 아이디",
		 comment = "사용자 아이디는 필수 입력 값입니다.",
		 required = true,
		 validator = {
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
		 validator = UserRoleValidator.class,
		 converter = StringToRoleConverter.class
	)
	private Role role;

	@ExcelProperty(
		 title = "사용자 성별",
		 validator = UseGenderValidator.class,
		 converter = StringToIntegerConverter.class
	)
	private Integer gender;

	@ExcelProperty(title = "이메일", validator = UserEmailValidator.class)
	private String email;

	@ExcelProperty(
		 title = "활성화",
		 comment = "활성화는 'Y'/'N' 입력만 가능합니다.",
		 required = true,
		 validator = BooleanStringYNValidator.class,
		 converter = StringYNToBooleanConverter.class
	)
	private boolean enabled;
}
