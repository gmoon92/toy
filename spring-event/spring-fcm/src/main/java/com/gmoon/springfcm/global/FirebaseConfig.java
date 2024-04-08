package com.gmoon.springfcm.global;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.gmoon.springfcm.messaging.FcmClient;
import com.gmoon.springfcm.messaging.FcmSender;
import com.gmoon.springfcm.messaging.NoOpFcmSender;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

	private final ResourceLoader resourceLoader;

	// @Value("${firebase.credentials}")
	// private Resource firebasePrivateKey;

	@Bean
	@ConditionalOnProperty(value = "firebase.enabled", havingValue = "1")
	public FirebaseApp firebaseApp(@Value("${firebase.credentials}") String path) {
		Resource firebasePrivateKey = resourceLoader.getResource(path);
		try (InputStream is = firebasePrivateKey.getInputStream()) {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(is))
				// .setProjectId(projectId) // optional
				.build();

			// return FirebaseApp.initializeApp(options, firebaseProperties.projectId); // optional
			return FirebaseApp.initializeApp(options);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	@ConditionalOnBean(FirebaseApp.class)
	public FcmClient fcmSender(FirebaseApp firebaseApp) {
		FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
		return new FcmSender(firebaseMessaging);
	}

	@Bean
	@ConditionalOnMissingBean(FcmClient.class)
	public FcmClient noopFcmSender() {
		return new NoOpFcmSender();
	}
}
