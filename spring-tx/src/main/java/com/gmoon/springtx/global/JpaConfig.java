package com.gmoon.springtx.global;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class JpaConfig {

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
		return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
	}

	/**
	 * {@link org.springframework.transaction.annotation.EnableTransactionManagement}
	 */
	@Configuration
	@RequiredArgsConstructor
	protected static class TransactionConfig {

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}

		/**
		 * <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html">Custom Advisor</a>
		 *
		 * @see ProxyTransactionManagementConfiguration#transactionAdvisor
		 * @see ProxyTransactionManagementConfiguration#transactionAttributeSource
		 * @see AnnotationTransactionAttributeSource
		 */
		// @Bean
		public Advisor customTransactionAdvisor(TransactionManager transactionManager) {
			TransactionAttributeSource txSource = getTransactionAttributeSource();

			AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
			advisor.setAdvice(new TransactionInterceptor(transactionManager, txSource));
			advisor.setExpression("execution(* com.gmoon..*(..)) && @within(org.springframework.stereotype.Service)");
			// advisor.setOrder(transactionAdvisor.getOrder() - 1);
			return advisor;
		}

		private TransactionAttributeSource getTransactionAttributeSource() {
			NameMatchTransactionAttributeSource txSource = new NameMatchTransactionAttributeSource();
			txSource.addTransactionalMethod("*", new DefaultTransactionAttribute());

			TransactionAttribute readOnly = readOnlyTransactionAttribute();
			txSource.addTransactionalMethod("get*", readOnly);
			txSource.addTransactionalMethod("find*", readOnly);
			txSource.addTransactionalMethod("is*", readOnly);
			txSource.addTransactionalMethod("check*", readOnly);
			txSource.addTransactionalMethod("exists*", readOnly);
			return txSource;
		}

		private TransactionAttribute readOnlyTransactionAttribute() {
			DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
			transactionAttribute.setReadOnly(true);
			return transactionAttribute;
		}
	}
}
