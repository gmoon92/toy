package com.gmoon.springjpapagination.users.userloginlog.infra;

import static com.gmoon.springjpapagination.users.userloginlog.domain.QUserLoginLog.*;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.springjpapagination.global.domain.CursorPagination;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLog;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLogRepository;
import com.gmoon.springjpapagination.users.userloginlog.dto.QUserLoginLogListVO_Data;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserLoginLogRepositoryAdapter implements UserLoginLogRepository {

	private final JpaUserLoginLogRepository repository;
	private final JPAQueryFactory factory;

	@Override
	public UserLoginLog save(UserLoginLog userLoginLog) {
		return repository.save(userLoginLog);
	}

	@Override
	public List<UserLoginLog> findAll() {
		return repository.findAll();
	}

	@Override
	public UserLoginLog get(String id) {
		return repository.findById(id)
			 .orElseThrow(EntityNotFoundException::new);
	}

	@Override
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

	private boolean hasNextPage(String cursor, Order cursorOrder) {
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
