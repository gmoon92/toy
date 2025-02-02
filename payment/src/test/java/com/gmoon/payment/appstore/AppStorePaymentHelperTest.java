package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.client.GetTransactionHistoryVersion;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import com.apple.itunes.storekit.verification.VerificationStatus;
import com.gmoon.payment.test.UnitTestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThat;

@Disabled
@Slf4j
@UnitTestCase(AppStorePaymentHelper.class)
class AppStorePaymentHelperTest {

	@Autowired
	private AppStorePaymentHelper helper;


	@DisplayName("서버 테스트 알람 요청")
	@Test
	void requestTestNotification() {
		try {
			AppStoreServerAPIClient client = helper.newAppStoreClient();
			SendTestNotificationResponse response = client.requestTestNotification();
			log.debug("{}", response);


		} catch (APIException | IOException e) {
			log.warn("", e);
		}
	}

	@DisplayName("서명된 결제 데이터 조회")
	@Test
	void apis() {
		String transactionId = "2000000838069198";

		TransactionInfoResponse response = helper.getTransactionInfo(transactionId);
		log.debug("response: {}", response.getSignedTransactionInfo());
	}

	@DisplayName("서명된 결제 데이터 검증 및 복호화")
	@Test
	void verifyAndDecodeTransaction() throws VerificationException {
		// appAppleId must be provided for the Production environment
		long appAppleId = 518615856L;
		String transactionId = "2000000838069198";

		JWSTransactionDecodedPayload payload = helper.verifyAndDecodeTransaction(appAppleId, transactionId);
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
		// appAppleId must be provided for the Production environment
		long appAppleId = 518615856L;
		boolean enableOnlineChecks = true;

		SignedDataVerifier signedPayloadVerifier = helper.getSignedDataVerifier(
			 appAppleId,
			 enableOnlineChecks
		);

		String transactionId = "2000000838069198";
		String notificationPayload = helper.getTransactionInfo(transactionId)
			 .getSignedTransactionInfo();

		try {
			ResponseBodyV2DecodedPayload payload = signedPayloadVerifier.verifyAndDecodeNotification(notificationPayload);
			log.debug("{}", payload);
		} catch (VerificationException e) {
			log.error("", e);
			assertThat(e.getStatus()).isEqualTo(VerificationStatus.INVALID_APP_IDENTIFIER);
		}
	}

	@DisplayName("영수증 조회")
	@Test
	void migration() throws Exception {
		String appReceipt = "MI...";

		ReceiptUtility receiptUtil = new ReceiptUtility();
		String transactionId = receiptUtil.extractTransactionIdFromAppReceipt(appReceipt);
		assumeThat(transactionId != null).isTrue();

		// migration
		TransactionHistoryRequest request = new TransactionHistoryRequest()
			 .sort(TransactionHistoryRequest.Order.ASCENDING)
			 .revoked(false)
			 .productTypes(List.of(TransactionHistoryRequest.ProductType.AUTO_RENEWABLE));

		HistoryResponse response = null;
		List<String> transactions = new LinkedList<>();
		AppStoreServerAPIClient client = helper.newAppStoreClient();
		do {
			String revision = response != null ? response.getRevision() : null;
			response = client.getTransactionHistory(transactionId, revision, request, GetTransactionHistoryVersion.V2);
			transactions.addAll(response.getSignedTransactions());
		} while (response.getHasMore());
		log.debug("{}", transactions);
	}

	@DisplayName("서명 생산")
	@Test
	void createSignature() {
		String productId = "<product_id>";
		String subscriptionOfferId = "<subscription_offer_id>";
		String appAccountToken = "<app_account_token>";

		assertThatCode(() -> helper.createSignature(productId, subscriptionOfferId, appAccountToken))
			 .doesNotThrowAnyException();
	}
}
