package com.gmoon.springpoi.excels.application;

import org.springframework.stereotype.Component;

import com.gmoon.springpoi.common.excel.handler.AbstractExcelRowHandler;
import com.gmoon.springpoi.users.application.UserService;
import com.gmoon.springpoi.users.domain.User;
import com.gmoon.springpoi.users.infra.UserRepository;
import com.gmoon.springpoi.users.model.ExcelUserVO;

import lombok.RequiredArgsConstructor;

/**
 * 엑셀 업로드 과정에서 {@link ExcelUserVO} 행 단위 데이터를
 * 사용자 도메인({@link UserService})에 반영하는 처리기입니다.
 *
 * <p>외부 데이터(엑셀) → 내부 도메인(User)으로 가져오는 성격이므로
 * 일반적으로 'Importer' 계열 네이밍이 자주 쓰입니다.
 */
@Component
@RequiredArgsConstructor
public class UserExcelCreateImporter extends AbstractExcelRowHandler<ExcelUserVO> {
	private final UserRepository userRepository;

	@Override
	protected void applyRow(ExcelUserVO excelVO) {
		User user = User.builder(
				  excelVO.getUsername(),
				  excelVO.getPassword(),
				  excelVO.getRole()
			 )
			 .gender(excelVO.getGender())
			 .email(excelVO.getEmail())
			 .enabled(excelVO.isEnabled())
			 .build();
		userRepository.save(user);
	}
}
