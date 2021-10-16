package com.gmoon.springasync.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(mode = AdviceMode.PROXY, proxyTargetClass = true)
@Configuration
public class SpringAsyncConfig {

}
