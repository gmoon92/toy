package com.gmoon.springcloudbus.common.config;

import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RemoteApplicationEventScan(basePackages = "com.gmoon")
public class SpringCloudBusConfig {
}
