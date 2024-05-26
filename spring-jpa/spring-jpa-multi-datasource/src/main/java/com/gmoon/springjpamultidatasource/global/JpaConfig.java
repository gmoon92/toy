package com.gmoon.springjpamultidatasource.global;

import java.lang.reflect.AnnotatedElement;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.gmoon.springjpamultidatasource.global.dsr.DynamicRoutingDatabaseSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * @see OpenEntityManagerInViewInterceptor
 * @see TransactionAttributeSource
 * @see SpringTransactionAnnotationParser
 * @see AnnotationTransactionAttributeSource#determineTransactionAttribute(AnnotatedElement)
 * @see ProxyTransactionManagementConfiguration
 * @see EnableTransactionManagement
 * @see TransactionAutoConfiguration
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
	 basePackages = "com.gmoon.**",
	 repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class
)
public class JpaConfig {

	/**
	 * @see HibernateJpaAutoConfiguration
	 * @see LocalEntityManagerFactoryBean
	 * @see org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration
	 * @see JpaBaseConfiguration
	 * @see JpaBaseConfiguration#entityManagerFactoryBuilder(JpaVendorAdapter, ObjectProvider, ObjectProvider)
	 */
	@Bean
	@DependsOn("routingDataSource")
	public EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer(DataSource routingDataSource) {
		return builder -> builder.dataSource(routingDataSource)
			 .persistenceUnit("default");
	}

	static class DataSourceConfig {

		@Bean
		@Primary
		@DependsOn({"masterDataSourceProperties", "slaveDataSourceProperties"})
		public DataSource routingDataSource(
			 DataSourceProperties masterDataSourceProperties,
			 DataSourceProperties slaveDataSourceProperties
		) {
			return new LazyConnectionDataSourceProxy(new DynamicRoutingDatabaseSource(
				 masterDataSourceProperties,
				 slaveDataSourceProperties
			));
		}

		@Bean
		@ConfigurationProperties("spring.datasource.master")
		public DataSourceProperties masterDataSourceProperties() {
			return new DataSourceProperties();
		}

		@Bean
		@ConfigurationProperties("spring.datasource.slave")
		public DataSourceProperties slaveDataSourceProperties() {
			return new DataSourceProperties();
		}
	}

	/**
	 * @see HibernateJpaAutoConfiguration
	 * @see TransactionManager
	 * @see org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration
	 * @see JpaBaseConfiguration
	 * @see JpaBaseConfiguration#transactionManager(ObjectProvider)
	 */
	static class TransactionConfig {

		@Bean
		public TransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}

		/**
		 * <p> {@link AnnotationTransactionAttributeSource} 보다 전에 처리하기 위함
		 *
		 * @see ProxyTransactionManagementConfiguration#transactionAdvisor
		 * @see ProxyTransactionManagementConfiguration#transactionAttributeSource
		 */
		@Bean
		public Advisor transactionAdvisor(
			 TransactionManager transactionManager,
			 BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor
		) {
			TransactionAttributeSource txSource = newTransactionAttributeSource();

			AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
			advisor.setAdvice(new TransactionInterceptor(transactionManager, txSource));
			advisor.setExpression("execution(* com.gmoon..*.*Service.*(..))");
			advisor.setOrder(transactionAdvisor.getOrder() - 1);
			return advisor;
		}

		private TransactionAttributeSource newTransactionAttributeSource() {
			NameMatchTransactionAttributeSource txSource = new NameMatchTransactionAttributeSource();
			txSource.addTransactionalMethod("*", new DefaultTransactionAttribute());

			DefaultTransactionAttribute readOnly = new DefaultTransactionAttribute();
			readOnly.setReadOnly(true);
			txSource.addTransactionalMethod("get*", readOnly);
			txSource.addTransactionalMethod("find*", readOnly);
			txSource.addTransactionalMethod("is*", readOnly);
			txSource.addTransactionalMethod("check*", readOnly);
			txSource.addTransactionalMethod("exists*", readOnly);
			return txSource;
		}
	}
}
