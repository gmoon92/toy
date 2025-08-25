package com.gmoon.springpoi.excels.application;

import org.springframework.stereotype.Service;

import com.gmoon.springpoi.common.excel.processor.AbstractExcelRowProcessor;
import com.gmoon.springpoi.users.application.UserService;
import com.gmoon.springpoi.users.model.ExcelUserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserExcelCreateService extends AbstractExcelRowProcessor<ExcelUserVO> {
	private final UserService userService;

	@Override
	protected void doProcess(ExcelUserVO excelVO) {
		userService.saveExcelUser(excelVO);
	}
}
