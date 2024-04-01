package com.gmoon.springfcm.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix = "firebase")
@ConstructorBinding
@RequiredArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class FirebaseProperties {

	public final String projectId;
	public final String serverKey;
	public final String credentials;
}
