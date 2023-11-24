package com.gmoon.batchinsert.accesslogs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#_statelesssession
@Slf4j
@RequiredArgsConstructor
public class AccessLogExcelDownloadRepositoryQueryImpl implements AccessLogExcelDownloadRepositoryQuery {

	private final EntityManager em;

	// https://docs.jboss.org/hibernate/core/3.5/reference/en/html/batch.html
	@Override
	public List<AccessLogExcelDownload> bulkSaveAllAtStatelessSession(List<AccessLog> accessLogs) {
		Session session = em.unwrap(Session.class);
		SessionFactory sessionFactory = session.getSessionFactory();

		try (StatelessSession statelessSession = sessionFactory.openStatelessSession()) {
			// Transaction tx = statelessSession.beginTransaction();

			List<AccessLogExcelDownload> result = new ArrayList<>(accessLogs.size());
			for (AccessLog accessLog : accessLogs) {
				AccessLogExcelDownload data = AccessLogExcelDownload.create(accessLog);

				statelessSession.insert(data);

				result.add(data);
			}

			// tx.commit();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Not saved excel download data", e);
		}
	}
}
