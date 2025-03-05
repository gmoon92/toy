package com.gmoon.payment.appstore.infra;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.gmoon.payment.global.config.PaymentConfig;
import com.gmoon.payment.test.Fixtures;
import com.gmoon.payment.test.UnitTestCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnabledIf(value = "#{'${service.payment.appstore.enabled}' == '1'}", loadContext = true)
@UnitTestCase(PaymentConfig.class)
class AppStoreClientTest {

	@Autowired
	private AppStoreClient client;

	@Disabled("APIException{httpStatusCode=404, apiError=4040007, apiErrorMessage='No App Store Server Notification URL found for provided app. Check that a URL is configured in App Store Connect for this environment.'}")
	@DisplayName("서버 테스트 알람 요청")
	@Test
	void requestTestNotification() {
		assertThatCode(() -> client.requestTestNotification())
			 .doesNotThrowAnyException();
	}

	@DisplayName("서명된 결제 데이터 검증 및 복호화")
	@Test
	void verifyAndDecodeTransaction() {
		String transactionId = Fixtures.AppStore.TRANSACTION_ID;

		JWSTransactionDecodedPayload payload = client.verifyAndDecodeTransaction(transactionId);
		log.info("payload: {}", payload);
		log.info("appAccountToken          : {}", payload.getAppAccountToken());
		log.info("environment              : {}", payload.getEnvironment());
		log.info("productId                : {}", payload.getProductId());
		log.info("bundleId                 : {}", payload.getBundleId());
		log.info("transactionId            : {}", payload.getTransactionId());
		log.info("originalTransactionId    : {}", payload.getOriginalTransactionId());
		log.info("purchaseDate             : {}", payload.getPurchaseDate());
		log.info("originalPurchaseDate     : {}", payload.getOriginalPurchaseDate());
		log.info("isUpgraded               : {}", payload.getIsUpgraded());
		log.info("expiresDate              : {}", payload.getExpiresDate());
		log.info("revocationDate           : {}", payload.getRevocationDate());

		log.info("offerIdentifier          : {}", payload.getOfferIdentifier());
		log.info("offerType                : {}", payload.getOfferType());
		log.info("offerDiscountType        : {}", payload.getOfferDiscountType());

		log.info("currency                 : {}", payload.getCurrency());
		log.info("price                    : {}", payload.getPrice());
		log.info("quantity                 : {}", payload.getQuantity());

		log.info("unknownFields            : {}", payload.getUnknownFields());
	}

	@Disabled("서버 알람 URL 설정 후 검증 진행 - 앱스토어 서버에서 영수증 데이터 필요.")
	@DisplayName("영수증 마이그레이션")
	@Test
	void migration() throws Exception {
		String appReceipt = "MI...";

		List<String> transactions = client.migration(appReceipt);
		log.debug("{}", transactions);
	}

	@DisplayName("서명 생산")
	@Test
	void createSignature() {
		String productId = "<product_id>";
		String subscriptionOfferId = "<subscription_offer_id>";
		String appAccountToken = "<app_account_token>";

		assertThatCode(() -> client.createSignature(productId, subscriptionOfferId, appAccountToken))
			 .doesNotThrowAnyException();
	}
}
