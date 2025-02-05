package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.model.SendTestNotificationResponse;
import com.apple.itunes.storekit.verification.VerificationException;
import com.apple.itunes.storekit.verification.VerificationStatus;
import com.gmoon.payment.test.Fixtures;
import com.gmoon.payment.test.UnitTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Disabled
@Slf4j
@UnitTestCase(AppStoreClient.class)
class AppStoreClientTest {

	@Autowired
	private AppStoreClient client;

	@DisplayName("서버 테스트 알람 요청")
	@Test
	void requestTestNotification() {
		try {
			SendTestNotificationResponse response = client.requestTestNotification();
			log.debug("{}", response);
		} catch (APIException | IOException e) {
			log.warn("", e);
		}
	}

	@DisplayName("서명된 결제 데이터 검증 및 복호화")
	@Test
	void verifyAndDecodeTransaction() throws VerificationException {
		var transactionId = Fixtures.AppStore.TRANSACTION_ID;
		var payload = client.verifyAndDecodeTransaction(transactionId);
		log.debug("payload: {}", payload);
		log.debug("appAccountToken          : {}", payload.getAppAccountToken());
		log.debug("environment              : {}", payload.getEnvironment());
		log.debug("productId                : {}", payload.getProductId());
		log.debug("bundleId                 : {}", payload.getBundleId());
		log.debug("transactionId            : {}", payload.getTransactionId());
		log.debug("originalTransactionId    : {}", payload.getOriginalTransactionId());
		log.debug("purchaseDate             : {}", payload.getPurchaseDate());
		log.debug("originalPurchaseDate     : {}", payload.getOriginalPurchaseDate());
		log.debug("isUpgraded               : {}", payload.getIsUpgraded());
		log.debug("expiresDate              : {}", payload.getExpiresDate());
		log.debug("revocationDate           : {}", payload.getRevocationDate());

		log.debug("offerIdentifier          : {}", payload.getOfferIdentifier());
		log.debug("offerType                : {}", payload.getOfferType());
		log.debug("offerDiscountType        : {}", payload.getOfferDiscountType());

		log.debug("currency                 : {}", payload.getCurrency());
		log.debug("price                    : {}", payload.getPrice());
		log.debug("quantity                 : {}", payload.getQuantity());

		log.debug("unknownFields            : {}", payload.getUnknownFields());
	}

	@DisplayName("서버 알람 데이터 검증")
	@Test
	void verifyNotificationPayload() {
		try {
			var transactionId = Fixtures.AppStore.TRANSACTION_ID;
			var payload = client.verifyAndDecodeNotification(transactionId);
			log.debug("{}", payload);
		} catch (VerificationException e) {
			log.error("", e);
			assertThat(e.getStatus()).isEqualTo(VerificationStatus.INVALID_APP_IDENTIFIER);
		}
	}

	@DisplayName("영수증 조회")
	@Test
	void migration() throws Exception {
		var appReceipt = "MI...";
		List<String> transactions = client.migration(appReceipt);

		log.debug("{}", transactions);
	}

	@DisplayName("서명 생산")
	@Test
	void createSignature() {
		var productId = "<product_id>";
		var subscriptionOfferId = "<subscription_offer_id>";
		var appAccountToken = "<app_account_token>";

		assertThatCode(() -> client.createSignature(productId, subscriptionOfferId, appAccountToken))
			 .doesNotThrowAnyException();
	}
}
