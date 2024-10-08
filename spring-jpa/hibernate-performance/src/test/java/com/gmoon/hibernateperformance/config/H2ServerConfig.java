package com.gmoon.hibernateperformance.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ConditionalOnProperty(value = "service.datasource.platform", havingValue = "h2")
@Configuration
public class H2ServerConfig {

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2TcpServer() {
		log.info("Initializing H2 TCP Server");
		try {
			return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
		} catch (SQLException e) {
			throw new RuntimeException("H2 tcp server config fail...", e.getCause());
		}
	}

	@Bean
	@ConfigurationProperties("spring.datasource.hikari")
	public DataSource dataSource() {
		log.info("Initializing Hikari DataSource");
		h2TcpServer();
		return new HikariDataSource();
	}
}
