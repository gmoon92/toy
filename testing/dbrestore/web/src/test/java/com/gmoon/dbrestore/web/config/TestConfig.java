package com.gmoon.dbrestore.web.config;

import com.gmoon.dbrestore.test.dbrestore.DatabaseRestorationConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(DatabaseRestorationConfig.class)
@Configuration
public class TestConfig {
}
