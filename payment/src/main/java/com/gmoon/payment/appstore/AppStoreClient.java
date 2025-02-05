package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.client.GetTransactionHistoryVersion;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.offers.PromotionalOfferSignatureCreator;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import com.gmoon.javacore.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppStoreClient implements InitializingBean {

	private final AppStoreProperties appStoreProperties;
	private final ResourceLoader resourceLoader;

	private AppStoreServerAPIClient apiClient;
	private SignedDataVerifier signedDataVerifier;

	private static String obtainTransactionId(String appReceipt) throws IOException {
		return new ReceiptUtility()
			 .extractTransactionIdFromAppReceipt(appReceipt);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("=================== app store prop ===================");
		log.debug("app store prop           : {}", appStoreProperties);
		log.debug("environment              : {}", appStoreProperties.getEnvironment());
		log.debug("root cert dir            : {}", appStoreProperties.rootCertDir());
		log.debug("bundle id                : {}", appStoreProperties.bundleId());
		log.debug("issuer id                : {}", appStoreProperties.issuerId());
		log.debug("app apple id             : {}", appStoreProperties.appAppleId());
		log.debug("private key id           : {}", appStoreProperties.privateKey().id());
		log.debug("private key file path    : {}", appStoreProperties.privateKey().filePath());
		log.debug("=================== app store prop ===================");
		var environment = appStoreProperties.getEnvironment();
		var bundleId = appStoreProperties.bundleId();
		var issuerId = appStoreProperties.issuerId();

		this.apiClient = new AppStoreServerAPIClient(
			 getSigningKey(),
			 appStoreProperties.privateKey().id(),
			 issuerId,
			 bundleId,
			 environment
		);

		var appAppleId = appStoreProperties.appAppleId();
		var enableOnlineChecks = false;
		this.signedDataVerifier = new SignedDataVerifier(
			 getRootCertInputStreams(),
			 bundleId,
			 appAppleId, // appAppleId must be provided for the Production environment
			 environment,
			 enableOnlineChecks // Whether to enable revocation checking and check expiration using the current date
		);
	}

	private String getSigningKey() {
		try {
			var location = appStoreProperties.privateKey().filePath();
			var resource = resourceLoader.getResource(location);
			return Files.readString(resource.getFile().toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<InputStream> getRootCertInputStreams() {
		var resource = resourceLoader.getResource(appStoreProperties.rootCertDir());
		try {
			var directory = resource.getFile();
			if (directory.isDirectory()) {
				try (Stream<Path> paths = Files.walk(directory.toPath())) {
					return paths
						 .filter(Files::isRegularFile)
						 .map(Path::toFile)
						 .map(FileUtils::convertFileToInputStream)
						 .collect(Collectors.toSet());
				}
			}

			throw new IllegalArgumentException("The resource is not a directory");
		} catch (IOException e) {
			throw new RuntimeException("not found root ca file.", e);
		}
	}

	public SendTestNotificationResponse requestTestNotification() throws APIException, IOException {
		return apiClient.requestTestNotification();
	}

	public JWSTransactionDecodedPayload verifyAndDecodeTransaction(String transactionId) throws VerificationException {
		var signedPayload = getSignedTransaction(transactionId);
		return signedDataVerifier.verifyAndDecodeTransaction(signedPayload);
	}

	public ResponseBodyV2DecodedPayload verifyAndDecodeNotification(String transactionId) throws VerificationException {
		var signedPayload = getSignedTransaction(transactionId);
		return signedDataVerifier.verifyAndDecodeNotification(signedPayload);
	}

	private String getSignedTransaction(String transactionId) {
		try {
			var response = apiClient.getTransactionInfo(transactionId);
			var signedPayload = response.getSignedTransactionInfo();
			log.debug("signed data      : {}", signedPayload);
			log.debug("unknown fields   : {}", response.getUnknownFields());
			return signedPayload;
		} catch (APIException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a promotional offer signature
	 *
	 * @param productId           The subscription product identifier
	 * @param subscriptionOfferId The subscription discount identifier
	 * @param appAccountToken     An optional string value that you define; may be an empty string
	 * @return The Base64 encoded signature
	 * @see <a href="https://developer.apple.com/documentation/storekit/in-app_purchase/original_api_for_in-app_purchase/subscriptions_and_offers/generating_a_signature_for_promotional_offers">Generating a signature for promotional offers</a>
	 */
	public String createSignature(String productId, String subscriptionOfferId, String appAccountToken) {
		var keyId = appStoreProperties.privateKey().id();
		var bundleId = appStoreProperties.bundleId();
		var encodedKey = getSigningKey();

		var signatureCreator = new PromotionalOfferSignatureCreator(
			 encodedKey,
			 keyId,
			 bundleId
		);

		var nonce = UUID.randomUUID();
		var timestamp = System.currentTimeMillis();
		var encodedSignature = signatureCreator.createSignature(
			 productId,
			 subscriptionOfferId,
			 appAccountToken,
			 nonce, // A one-time UUID value that your server generates. Generate a new nonce for every signature.
			 timestamp // A timestamp your server generates in UNIX time format, in milliseconds. The timestamp keeps the offer active for 24 hours.
		);
		log.debug(encodedSignature);
		return encodedSignature;
	}

	public List<String> migration(String appReceipt) throws Exception {
		var transactionId = obtainTransactionId(appReceipt);

		// migration
		var request = new TransactionHistoryRequest()
			 .sort(TransactionHistoryRequest.Order.ASCENDING)
			 .revoked(false)
			 .productTypes(List.of(TransactionHistoryRequest.ProductType.AUTO_RENEWABLE));

		HistoryResponse response = null;
		List<String> transactions = new LinkedList<>();
		do {
			var revision = response != null ? response.getRevision() : null;
			response = apiClient.getTransactionHistory(
				 transactionId,
				 revision,
				 request,
				 GetTransactionHistoryVersion.V2
			);
			transactions.addAll(response.getSignedTransactions());
		} while (Boolean.TRUE.equals(response.getHasMore()));
		return transactions;
	}
}
