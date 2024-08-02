package com.gmoon.dbrestore.web.config;

import com.gmoon.dbrestore.test.dbrestore.DatabaseRestoreConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(DatabaseRestoreConfig.class)
@Configuration
public class TestConfig {
}
