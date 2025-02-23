package com.gmoon.springfcm.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gmoon.javacore.util.JacksonUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
@SpringBootTest
class FcmClientTest {

	@Autowired
	private FcmClient sender;

	@Autowired
	private ResourceLoader resourceLoader;

	@Value("${firebase.enabled}")
	private boolean enabled;

	@Value("${firebase.credentials}")
	private String credentialsPath;

	@Disabled("기존 레거시 API 사용."
		 + "Firebase Cloud Messaging API 에서 6월까지 지원.")
	@Test
	void pushByLegacyApi() {
		String clientToken = "cKQ2XcbRxBNufMfvnpYJ2h:APA91bFo4UC1jzFi_iSnvDqbseM4H0Bv_T0pEfDinN4tZ9obMhpvlOnCusdK3RVs5CoZfBVUctbhUMIbps_KGIYkwZT6f_L5y477DNid5C4oUUV5joGECuuVeyUAtgcfxcgO60Ev9D3v";

		MessageRequestVO requestVO = MessageRequestVO.builder()
			 .to(clientToken)
			 .build();

		String response = pushByLegacyApi(requestVO);

		log.debug("response: {}", response);
		assertThat(response).isNotBlank();
	}

	private String pushByLegacyApi(Serializable requestVO) {
		try {
			log.debug("send message...");
			final String serverKey = "AAAAyYjB7sc:APA91bHe6FUh_Z2TFjDg3TyERMhqgcahWn1Bb4Y1S-p-mFD0BlRivRKzSNbzFsCo2mZbimOrWqvWe88rlCZl21P-LVuA0-8hfvGeUKoPNeUOaMOVbNysV3ldJhhEjux6A_ErSRbLzTAD";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "key=" + serverKey);
			String body = JacksonUtils.toString(requestVO);

			return new RestTemplate()
				 .postForObject(
					  "https://fcm.googleapis.com/fcm/send",
					  new HttpEntity<>(body, headers),
					  String.class
				 );
		} catch (Exception e) {
			throw new RuntimeException("push message fail.", e);
		}
	}

	@Builder
	static class MessageRequestVO implements Serializable {

		private static final long serialVersionUID = -2527398867922148105L;

		@JsonProperty
		private String to;

		@JsonProperty
		private String priority;

		@JsonProperty("time_to_live")
		private long ttl;

		@JsonProperty
		private Map<String, String> data;

		@JsonProperty
		private Map<String, String> notification;
	}

	@Test
	void push() {
		String clientToken = "cKQ2XcbRxBNufMfvnpYJ2h:APA91bFo4UC1jzFi_iSnvDqbseM4H0Bv_T0pEfDinN4tZ9obMhpvlOnCusdK3RVs5CoZfBVUctbhUMIbps_KGIYkwZT6f_L5y477DNid5C4oUUV5joGECuuVeyUAtgcfxcgO60Ev9D3v";

		Message message = Message.builder()
			 .setToken(clientToken)
			 .putData("action", "update")
			 .setNotification(Notification.builder()
				  .setTitle("최신 업데이트를 진행합니다.")
				  .setBody("gmoon-app-24.3 업데이트를 진행합니다.")
				  .build())
			 .setWebpushConfig(
				  WebpushConfig.builder()
					   .putHeader("Urgency", "high") // priority
					   .build())
			 .setAndroidConfig(
				  AndroidConfig.builder()
					   .setTtl(Duration.ofSeconds(100).toMillis())
					   .setPriority(AndroidConfig.Priority.HIGH)
					   .build()
			 )
			 .build();

		String response = sender.push(message);

		log.debug("response: {}", response);
		assertThat(response).isNotBlank();
	}

	@DisplayName("Admin SDK를 사용하는 경우가 아니라면 "
		 + "OAuth 2.0 액세스 토큰을 발급하여 전송 요청에 추가해야 한다.")
	@Test
	void withOAuthToken() {
		assumeTrue(enabled);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + getAccessToken());

		String payload = "{\n"
			 + "   \"message\":{\n"
			 + "      \"token\":\"cKQ2XcbRxBNufMfvnpYJ2h:APA91bFo4UC1jzFi_iSnvDqbseM4H0Bv_T0pEfDinN4tZ9obMhpvlOnCusdK3RVs5CoZfBVUctbhUMIbps_KGIYkwZT6f_L5y477DNid5C4oUUV5joGECuuVeyUAtgcfxcgO60Ev9D3v\",\n"
			 + "      \"notification\":{\n"
			 + "        \"title\":\"FCM Message\",\n"
			 + "        \"body\":\"This is an FCM notification message!\"\n"
			 + "      }\n"
			 + "   }\n"
			 + "}";

		String response = new RestTemplate()
			 .postForObject(
				  "https://fcm.googleapis.com/v1/projects/gmoon-app/messages:send",
				  new HttpEntity<>(payload, headers),
				  String.class
			 );

		log.debug("response: {}", response);
		assertThat(response).isNotBlank();
	}

	private String getAccessToken() {
		try {
			GoogleCredentials credentials = obtainGoogleCredentials();
			credentials.refresh();
			// credentials.refreshAccessToken();

			return credentials.getAccessToken().getTokenValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private GoogleCredentials obtainGoogleCredentials() {
		Resource credentials = resourceLoader.getResource(credentialsPath);
		try (InputStream is = credentials.getInputStream()) {
			return GoogleCredentials.fromStream(is)
				 .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
		} catch (Exception e) {
			throw new RuntimeException("google credentials file error.", e);
		}
	}
}
