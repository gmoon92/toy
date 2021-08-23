package com.gmoon.hibernateenvers.global.config;

import com.gmoon.hibernateenvers.global.properties.HibernateProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.gmoon.**",
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableTransactionManagement
public class JpaConfig {

  private final static String PERSISTENCE_UNIT_NAME = "defaultUnit";

  @Autowired
  private DataSource dataSource;

  @Autowired
  private HibernateProperties hibernateProperties;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
    em.setDataSource(dataSource);
    em.setPackagesToScan(new String[]{ "com.gmoon.**.domain" });
    em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    em.setJpaPropertyMap(hibernateProperties());

    log.debug("dataSource : {}", dataSource);
    log.debug("hibernateProperties() : {}", hibernateProperties());
    return em;
  }

  public Map<String, String> hibernateProperties() {
    Map<String, String> hibernatePropMap = hibernateProperties.getHibernate();
    Map<String, String> map = new HashMap<>();
    map.put("hibernate.hbm2ddl.auto", hibernatePropMap.get("hbm2ddl_auto"));
    map.put("hibernate.dialect", hibernatePropMap.get("dialect"));
    map.put("hibernate.show_sql", hibernatePropMap.get("show_sql"));
    map.put("hibernate.format_sql", hibernatePropMap.get("format_sql"));
    map.put("hibernate.use_sql_comments", hibernatePropMap.get("use_sql_comments"));
    map.put("org.hibernate.envers.audit_table_suffix", hibernatePropMap.get("audit_table_suffix"));
    return map;
  }

  @Bean
  public JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
    final JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

}
