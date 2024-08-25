package com.gmoon.batchinsert.accesslogs.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#_statelesssession
@Slf4j
@RequiredArgsConstructor
public class AccessLogExcelDownloadRepositoryQueryImpl implements AccessLogExcelDownloadRepositoryQuery {

	private final JPAQueryFactory factory;
	private final EntityManager em;

	// https://docs.jboss.org/hibernate/core/3.5/reference/en/html/batch.html
	@Transactional
	@Override
	public List<AccessLogExcelDownload> bulkSaveAllAtStatelessSession(List<AccessLog> accessLogs) {
		Session session = em.unwrap(Session.class);
		SessionFactory sessionFactory = session.getSessionFactory();

		try (StatelessSession statelessSession = sessionFactory.openStatelessSession()) {
			Transaction tx = statelessSession.beginTransaction();

			List<AccessLogExcelDownload> result = new ArrayList<>(accessLogs.size());
			for (AccessLog accessLog : accessLogs) {
				AccessLogExcelDownload data = AccessLogExcelDownload.create(accessLog);

				statelessSession.insert(data);

				result.add(data);
			}

			tx.commit();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Not saved excel download data", e);
		}
	}

	@Override
	public List<AccessLogExcelDownload> bulkSaveAllAtQueryDsl(List<AccessLog> accessLogs) {
		List<AccessLogExcelDownload> result = new ArrayList<>(accessLogs.size());

		for (AccessLog accessLog : accessLogs) {
			QAccessLogExcelDownload qAccessLogExcelDownload = QAccessLogExcelDownload.accessLogExcelDownload;
			AccessLogExcelDownload data = AccessLogExcelDownload.create(accessLog);
			JPAInsertClause clause = new JPAInsertClause(em, qAccessLogExcelDownload)
				 .columns(
					  qAccessLogExcelDownload.id,
					  qAccessLogExcelDownload.username,
					  qAccessLogExcelDownload.ip,
					  qAccessLogExcelDownload.os,
					  qAccessLogExcelDownload.attemptDt
				 ).values(
					  data.getId(),
					  data.getUsername(),
					  data.getIp(),
					  data.getOs(),
					  data.getAttemptDt()
				 );

			clause.execute();
		}

		return result;
	}
}
