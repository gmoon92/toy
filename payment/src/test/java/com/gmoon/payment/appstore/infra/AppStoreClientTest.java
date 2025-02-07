package com.gmoon.payment.appstore.infra;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.gmoon.payment.test.Fixtures;
import com.gmoon.payment.test.UnitTestCase;

import lombok.extern.slf4j.Slf4j;

@Disabled
@Slf4j
@UnitTestCase(AppStoreClient.class)
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

	@Disabled("서버 알람 URL 설정 후 검증 진행 - 앱스토어 서버에서 영수증 데이터 필요.")
	@DisplayName("영수증 조회")
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
