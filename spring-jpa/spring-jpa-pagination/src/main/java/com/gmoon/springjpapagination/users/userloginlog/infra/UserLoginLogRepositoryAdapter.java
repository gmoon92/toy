package com.gmoon.springjpapagination.users.userloginlog.infra;

import static com.gmoon.springjpapagination.users.userloginlog.domain.QUserLoginLog.*;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.javacore.util.CollectionUtils;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLog;
import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLogRepository;
import com.gmoon.springjpapagination.users.userloginlog.dto.QUserLoginLogListVO_Data;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		UserLoginLogListVO.Cursor cursor = listVO.getCursor();
		Order cursorOrder = Order.DESC;

		JPAQuery<UserLoginLogListVO.Data> query = factory
			 .select(
				  new QUserLoginLogListVO_Data(
					   userLoginLog.id,
					   userLoginLog.username, userLoginLog.accessDevice,
					   userLoginLog.attemptAt, userLoginLog.attemptIp, userLoginLog.succeed)
			 )
			 .from(userLoginLog)
			 .where(cursorPagination(cursor, cursorOrder))
			 .orderBy(userLoginLog.attemptAt.desc(), userLoginLog.id.desc())
			 .limit(listVO.getPageSize());

		List<UserLoginLogListVO.Data> list = query.fetch();
		listVO.setList(list);

		UserLoginLogListVO.Cursor lastRowCursor = getLastRowCursor(list);
		listVO.setCursor(lastRowCursor);
		listVO.setHasNextPage(hasNextPage(lastRowCursor, cursorOrder));
		return listVO;
	}

	private boolean hasNextPage(UserLoginLogListVO.Cursor cursor, Order cursorOrder) {
		log.info("last row cursor: {}", cursor);
		return factory.selectOne()
			 .from(userLoginLog)
			 .where(cursorPagination(cursor, cursorOrder))
			 .orderBy(userLoginLog.attemptAt.desc(), userLoginLog.id.desc())
			 .fetchOne() != null;
	}

	private UserLoginLogListVO.Cursor getLastRowCursor(List<UserLoginLogListVO.Data> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		UserLoginLogListVO.Data lastData = list.get(list.size() - 1);
		return new UserLoginLogListVO.Cursor(
			 lastData.getId(),
			 Instant.ofEpochMilli(lastData.getAttemptTime())
		);
	}

	private Predicate cursorPagination(UserLoginLogListVO.Cursor cursor, Order cursorOrder) {
		boolean isFirstPage = cursor == null;
		if (isFirstPage) {
			return null;
		}

		String id = cursor.id();
		Instant attemptAt = cursor.attemptAt();
		if (Order.ASC == cursorOrder) {
			return userLoginLog.attemptAt.gt(attemptAt)
				 .orAllOf(
					  userLoginLog.attemptAt.eq(attemptAt),
					  userLoginLog.id.gt(id)
				 );
		}

		if (Order.DESC == cursorOrder) {
			return userLoginLog.attemptAt.lt(attemptAt)
				 .orAllOf(
					  userLoginLog.attemptAt.eq(attemptAt),
					  userLoginLog.id.lt(id)
				 );
		}

		throw new RuntimeException("must be specify the order of cursor data.");
	}

	/**
	 * @implNote 아래 방식으로 사용 X, 결과가 달라짐.
	 * */
	@Deprecated(since = "2", forRemoval = true)
	private StringExpression cursor() {
		return Expressions.stringTemplate("CONCAT({0}, LPAD({1}, 50, '0'))", userLoginLog.attemptAt, userLoginLog.id);
	}
}
