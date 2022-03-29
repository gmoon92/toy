package com.gmoon.springjpapagination.userloginlog;

import static com.gmoon.springjpapagination.userloginlog.QUserLoginLog.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpapagination.common.CursorPagination;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginLogService {
	private final JPAQueryFactory factory;

	@Transactional(readOnly = true)
	public UserLoginLogListVO getUserLoginLogListVO(UserLoginLogListVO listVO) {
		String cursor = listVO.getCursor();
		Order cursorOrder = Order.DESC;

		JPAQuery<UserLoginLogListVO.Data> query = factory
			.select(
				new QUserLoginLogListVO_Data(userLoginLog.username, userLoginLog.accessDevice,
					userLoginLog.attemptIp, userLoginLog.succeed,
					cursor())
			)
			.from(userLoginLog)
			.where(cursorPagination(cursor, cursorOrder))
			.orderBy(userLoginLog.attemptDt.desc(), userLoginLog.id.desc())
			.limit(listVO.getPageSize());

		List<UserLoginLogListVO.Data> list = query.fetch();
		listVO.setList(list);

		String lastRowCursor = getLastRowCursor(list);
		listVO.setCursor(lastRowCursor);
		listVO.setHasNextPage(hasNextPage(lastRowCursor, cursorOrder));
		return listVO;
	}

	@Transactional(readOnly = true)
	public boolean hasNextPage(String cursor, Order cursorOrder) {
		return factory.selectOne()
			.from(userLoginLog)
			.where(cursorPagination(cursor, cursorOrder))
			.orderBy(userLoginLog.attemptDt.desc(), userLoginLog.id.desc())
			.fetchOne() != null;
	}

	private String getLastRowCursor(List<UserLoginLogListVO.Data> list) {
		return list.stream()
			.map(UserLoginLogListVO.Data::getCursor)
			.reduce((data, other) -> other)
			.orElse(CursorPagination.EMPTY_CURSOR);
	}

	private BooleanExpression cursorPagination(String cursor, Order cursorOrder) {
		boolean isFirstPage = StringUtils.isBlank(cursor);
		if (isFirstPage) {
			return null;
		}

		if (Order.ASC == cursorOrder) {
			return cursor().gt(cursor);
		}

		if (Order.DESC == cursorOrder) {
			return cursor().lt(cursor);
		}

		throw new RuntimeException("must be specify the order of cursor data.");
	}

	private StringExpression cursor() {
		return Expressions.stringTemplate("CONCAT({0}, LPAD({1}, 50, '0'))", userLoginLog.attemptDt, userLoginLog.id);
	}
}
