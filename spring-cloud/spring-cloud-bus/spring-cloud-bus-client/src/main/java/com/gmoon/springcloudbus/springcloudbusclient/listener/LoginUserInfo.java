package com.gmoon.springcloudbus.springcloudbusclient.listener;

public record LoginUserInfo(
	 String username,
	 String ipAddress,
	 long timestamp,
	 String originService
) {
}
