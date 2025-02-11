package com.gmoon.payment.appstore.infra;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.client.GetTransactionHistoryVersion;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.offers.PromotionalOfferSignatureCreator;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import com.gmoon.payment.appstore.exception.AppStoreApiException;
import com.gmoon.payment.appstore.exception.AppStoreCertificateException;
import com.gmoon.payment.appstore.exception.AppStoreIOException;
import com.gmoon.payment.appstore.exception.AppStoreVerificationException;
import com.gmoon.payment.global.properties.AppStoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class AppStoreClient implements InitializingBean {

	private final AppStoreProperties appStoreProperties;
	private final ResourceLoader resourceLoader;

	private AppStoreServerAPIClient apiClient;
	private SignedDataVerifier signedDataVerifier;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("app store prop ==========================");
		long appAppleId = appStoreProperties.appAppleId();
		String bundleId = appStoreProperties.bundleId();
		Environment environment = appStoreProperties.getEnvironment();
		String issuerId = appStoreProperties.issuerId();
		AppStoreProperties.PrivateKey privateKey = appStoreProperties.privateKey();
		log.info("appAppleId         : {}", appAppleId);
		log.info("bundleId           : {}", bundleId);
		log.info("environment        : {}", environment);
		log.info("issuerId           : {}", issuerId);
		log.info("privateKey         : {}", privateKey);
		log.info("app store prop ==========================");

		this.apiClient = new AppStoreServerAPIClient(
			 getSigningKey(),
			 privateKey.id(),
			 issuerId,
			 bundleId,
			 environment
		);

		boolean enableOnlineChecks = false;
		Set<InputStream> rootCertInputStreams = new LinkedHashSet<>();
		try {
			rootCertInputStreams = getRootCertInputStreams();
			this.signedDataVerifier = new SignedDataVerifier(
				 rootCertInputStreams,
				 bundleId,
				 appAppleId, // appAppleId must be provided for the Production environment
				 environment,
				 enableOnlineChecks // Whether to enable revocation checking and check expiration using the current date
			);
		} finally {
			for (InputStream inputStream : rootCertInputStreams) {
				inputStream.close();
			}
		}
	}

	private Set<InputStream> getRootCertInputStreams() {
		Resource resource = resourceLoader.getResource(appStoreProperties.rootCertDir());
		try {
			File directory = resource.getFile();
			if (directory.isDirectory()) {
				try (Stream<Path> paths = Files.walk(directory.toPath())) {
					return paths
						 .filter(Files::isRegularFile)
						 .map(Path::toFile)
						 .map(file -> {
							 try {
								 return new FileInputStream(file);
							 } catch (FileNotFoundException e) {
								 throw new AppStoreCertificateException(e, "Not found root cert file.");
							 }
						 })
						 .collect(Collectors.toSet());
				}
			}

			throw new AppStoreCertificateException("The resource is not a directory.");
		} catch (IOException e) {
			throw new AppStoreCertificateException(e, "root cert file I/O error.");
		}
	}

	public JWSTransactionDecodedPayload verifyAndDecodeTransaction(String transactionId) {
		String notificationPayload = getTransactionInfo(transactionId)
			 .getSignedTransactionInfo();

		try {
			return signedDataVerifier.verifyAndDecodeTransaction(notificationPayload);
		} catch (VerificationException e) {
			throw new AppStoreVerificationException(e);
		}
	}

	private TransactionInfoResponse getTransactionInfo(String transactionId) {
		try {
			TransactionInfoResponse response = apiClient.getTransactionInfo(transactionId);
			log.debug("response: {}", response.getSignedTransactionInfo());
			return response;
		} catch (APIException e) {
			throw new AppStoreApiException(e);
		} catch (IOException e) {
			throw new AppStoreIOException(e);
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
		String keyId = appStoreProperties.privateKey().id();
		String bundleId = appStoreProperties.bundleId();
		String encodedKey = getSigningKey();

		PromotionalOfferSignatureCreator signatureCreator = new PromotionalOfferSignatureCreator(
			 encodedKey,
			 keyId,
			 bundleId
		);

		UUID nonce = UUID.randomUUID();
		long timestamp = System.currentTimeMillis();
		String encodedSignature = signatureCreator.createSignature(
			 productId,
			 subscriptionOfferId,
			 appAccountToken,
			 nonce, // A one-time UUID value that your server generates. Generate a new nonce for every signature.
			 timestamp
			 // A timestamp your server generates in UNIX time format, in milliseconds. The timestamp keeps the offer active for 24 hours.
		);
		log.debug("signature: {}", encodedSignature);
		return encodedSignature;
	}

	private String getSigningKey() {
		try {
			String location = appStoreProperties.privateKey().filePath();
			Resource resource = resourceLoader.getResource(location);
			return Files.readString(resource.getFile().toPath());
		} catch (IOException e) {
			throw new AppStoreCertificateException(e, "not found private key.");
		}
	}

	public SendTestNotificationResponse requestTestNotification() {
		try {
			SendTestNotificationResponse response = apiClient.requestTestNotification();
			log.debug("notification response: {}", response);
			return response;
		} catch (APIException e) {
			throw new AppStoreApiException(e);
		} catch (IOException e) {
			throw new AppStoreIOException(e);
		}
	}

	public List<String> migration(String appReceipt) throws Exception {
		ReceiptUtility receiptUtil = new ReceiptUtility();
		String transactionId = receiptUtil.extractTransactionIdFromAppReceipt(appReceipt);

		// migration
		TransactionHistoryRequest request = new TransactionHistoryRequest()
			 .sort(TransactionHistoryRequest.Order.ASCENDING)
			 .revoked(false)
			 .productTypes(List.of(TransactionHistoryRequest.ProductType.AUTO_RENEWABLE));

		HistoryResponse response = null;
		List<String> transactions = new LinkedList<>();
		do {
			String revision = response != null ? response.getRevision() : null;
			response = apiClient.getTransactionHistory(transactionId, revision, request,
				 GetTransactionHistoryVersion.V2);
			transactions.addAll(response.getSignedTransactions());
		} while (response.getHasMore());
		return transactions;
	}
}
