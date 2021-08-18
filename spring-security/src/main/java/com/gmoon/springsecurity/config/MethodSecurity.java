package com.gmoon.springsecurity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true,
        prePostEnabled = true,
        jsr250Enabled = true)
public class MethodSecurity {
}
