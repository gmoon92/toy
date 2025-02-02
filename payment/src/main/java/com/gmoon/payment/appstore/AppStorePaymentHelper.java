package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.TransactionInfoResponse;
import com.apple.itunes.storekit.offers.PromotionalOfferSignatureCreator;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import com.gmoon.javacore.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppStorePaymentHelper {

	private final AppStoreProperties appStoreProperties;
	private final ResourceLoader resourceLoader;

	public AppStoreServerAPIClient newAppStoreClient() {
		log.debug("app store prop: {}", appStoreProperties);
		return new AppStoreServerAPIClient(
			 getSigningKey(),
			 appStoreProperties.privateKey().id(),
			 appStoreProperties.issuerId(),
			 appStoreProperties.bundleId(),
			 appStoreProperties.environment().value
		);
	}

	private String getSigningKey() {
		try {
			String location = appStoreProperties.privateKey().filePath();
			Resource resource = resourceLoader.getResource(location);
			return Files.readString(resource.getFile().toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public JWSTransactionDecodedPayload verifyAndDecodeTransaction(long appAppleId, String transactionId) throws VerificationException {
		String notificationPayload = getTransactionInfo(transactionId)
			 .getSignedTransactionInfo();

		return getSignedDataVerifier(appAppleId, false)
			 .verifyAndDecodeTransaction(notificationPayload);
	}

	public TransactionInfoResponse getTransactionInfo(String transactionId) {
		AppStoreServerAPIClient client = newAppStoreClient();

		try {
			TransactionInfoResponse response = client.getTransactionInfo(transactionId);
			log.debug("response: {}", response.getSignedTransactionInfo());
			return response;
		} catch (APIException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param appAppleId         The unique identifier of the app in the App Store.
	 * @param enableOnlineChecks Whether to enable revocation checking and check expiration using the current date
	 * @apiNote A verifier and decoder class designed to decode signed data from the App Store.
	 */
	public SignedDataVerifier getSignedDataVerifier(long appAppleId, boolean enableOnlineChecks) {
		String bundleId = appStoreProperties.bundleId();
		Environment environment = appStoreProperties.environment().value;

		Set<InputStream> rootCAs = getRootCAFiles();
		return new SignedDataVerifier(
			 rootCAs,
			 bundleId,
			 appAppleId, // appAppleId must be provided for the Production environment
			 environment,
			 enableOnlineChecks
		);
	}

	private Set<InputStream> getRootCAFiles() {
		Resource resource = resourceLoader.getResource(appStoreProperties.rootCaDirectoryPath());
		try {
			File directory = resource.getFile();
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
			 timestamp // A timestamp your server generates in UNIX time format, in milliseconds. The timestamp keeps the offer active for 24 hours.
		);
		log.debug(encodedSignature);
		return encodedSignature;
	}
}
