package com.gmoon.springjpalock.global;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.RollbackException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseJpaTestCase {

	@PersistenceUnit
	protected EntityManagerFactory factory;

	protected <T> void executeQuery(Function<EntityManager, T> action,
		 Consumer<PersistenceException> exceptionHandler) {
		EntityManager em = factory.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			boolean execute = false;
			while (!tx.isActive() && !execute) {
				execute = true;
				log.info("[Step1-{}] open transaction.", tx.hashCode());
				tx.begin();

				T merged = action.apply(em);
				log.info("[Step2-{}] merged.", tx.hashCode());

				tx.commit();
				log.info("[Step3-{}] commit.", tx.hashCode());
			}
		} catch (RollbackException | OptimisticLockException e) {
			tx.rollback();
			exceptionHandler.accept(e);
			throw e;
		} finally {
			em.close();
		}
	}
}
