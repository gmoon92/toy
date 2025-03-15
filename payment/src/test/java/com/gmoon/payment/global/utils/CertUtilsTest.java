package com.gmoon.payment.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CertUtilsTest {

	/**
	 * @apiNote ASN.1 DER encoding is a tag, length, value encoding system for each element.
	 * `X.509` 인증서는 `ASN.1 형식으로 정의된 데이터 구조이며, DER(순수 바이너리) 또는 PEM(Base64 인코딩)으로 저장됩니다.
	 * `tbsCertificate`는 인증서의 서명 전 데이터(ASN.1 DER 인코딩된 바이너리)입니다.
	 */
	@Test
	void loadCertificate() {
		assertAll(
			 () -> {
				 X509Certificate derCert = loadCertificate("appstore/cert/root", "AppleIncRootCertificate.cer");
				 printCertFields(derCert);
			 },
			 () -> {
				 X509Certificate pemCert = loadCertificate("alipay/cert", "_.alipay.com.pem");
				 printCertFields(pemCert);
			 }
		);
	}

	@Test
	void print() {
		String certDirPath = "appstore/cert/root";
		assertAll(
			 () -> printCertFields(loadCertificate(certDirPath, "AppleIncRootCertificate.cer")),
			 () -> printCertFields(loadCertificate(certDirPath, "AppleIncRootCertificate.cer")),
			 () -> printCertFields(loadCertificate(certDirPath, "AppleComputerRootCertificate.cer")),
			 () -> printCertFields(loadCertificate(certDirPath, "AppleRootCA-G2.cer")),
			 () -> printCertFields(loadCertificate(certDirPath, "AppleRootCA-G3.cer")),
			 () -> printCertFields(loadCertificate("alipay/cert", "_.alipay.com.pem"))
		);
	}

	private void printCertFields(X509Certificate certificate) {
		try {
			byte[] tbsCertificate = certificate.getTBSCertificate();
			try (ASN1InputStream asn1InputStream = new ASN1InputStream(tbsCertificate)) {
				ASN1Primitive asn1Primitive = asn1InputStream.readObject();
				TBSCertificate tbsCert = TBSCertificate.getInstance(asn1Primitive);

				log.info("Version: {}", tbsCert.getVersionNumber());
				log.info("Serial Number: {}", tbsCert.getSerialNumber().getValue().toString(16));
				log.info("Signature Algorithm: {}", tbsCert.getSignature().getAlgorithm().getId());
				log.info("Issuer: {}", tbsCert.getIssuer().toString());
				log.info("Validity: [{} to {}]", tbsCert.getStartDate(), tbsCert.getEndDate());
				log.info("Subject: {}", tbsCert.getSubject().toString());
				log.info("Public Key Algorithm: {}", tbsCert.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm().getId());
				log.info("Extensions: {}", (tbsCert.getExtensions() != null ? tbsCert.getExtensions().toString() : "None"));
			}
		} catch (Exception e) {
			throw new RuntimeException("Invalid certificate file.", e);
		}
	}

	@DisplayName("유효한 인증서 인지")
	@Test
	void isCertificateValid() {
		final var certDirPath = "appstore/cert/root";
		final var filename = "AppleIncRootCertificate.cer";
		final var utc = ZoneOffset.UTC;
		final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		assertTrue(
			 CertUtils.isCertificateValid(
				  loadCertificate(certDirPath, filename),
				  LocalDate.parse("2035-02-09", formatter).atStartOfDay().toInstant(utc)
			 )
		);

		assertFalse(
			 CertUtils.isCertificateValid(
				  loadCertificate(certDirPath, filename),
				  LocalDate.parse("2035-02-10", formatter).atStartOfDay().toInstant(utc)
			 )
		);
	}

	/**
	 * @apiNote <a href="https://datatracker.ietf.org/doc/html/rfc5280#section-4.1">RFC5280 Basic Certificate Fields</a>
	 */
	@Test
	void basicCertificateFields() throws Exception {
		String certDirPath = "appstore/cert/root";
		X509Certificate certificate = loadCertificate(certDirPath, "AppleIncRootCertificate.cer");

		byte[] tbsCertificate = certificate.getTBSCertificate();
		try (ASN1InputStream asn1InputStream = new ASN1InputStream(tbsCertificate)) {
			ASN1Primitive asn1Primitive = asn1InputStream.readObject();
			log.info("Raw TBSCertificate: {}", asn1Primitive.toString());

			// TBSCertificate 파싱
			TBSCertificate tbsCert = TBSCertificate.getInstance(asn1Primitive);

			// 1. 버전 확인 (X.509 v1(0), v2(1), v3(2))
			int version = tbsCert.getVersionNumber();
			log.info("[0] Version: {}", version);
			assertEquals(3, version, "Expected X.509 v3 certificate");

			// 2. 시리얼 넘버 로깅
			BigInteger serialNumber = tbsCert.getSerialNumber().getValue();
			log.info("[1] Serial Number: {}", serialNumber.toString(16));

			// 3. 서명 알고리즘 확인
			AlgorithmIdentifier signatureAlgorithm = tbsCert.getSignature();
			String algorithmOID = signatureAlgorithm.getAlgorithm().getId();
			log.info("[2] Signature Algorithm OID: {}", algorithmOID);
			assertEquals("1.2.840.113549.1.1.5", algorithmOID, "Expected SHA-1 with RSA Encryption");

			// 4. 발급자 확인
			X500Name issuer = tbsCert.getIssuer();
			log.info("[3] Issuer: {}", issuer.toString());
			assertTrue(issuer.toString().contains("Apple Root CA"), "Expected Apple Root CA");

			// 5. 유효 기간 확인
			Time notBefore = tbsCert.getStartDate();
			Time notAfter = tbsCert.getEndDate();
			log.info("[4] validity");
			log.info("    Valid From : {}", notBefore);
			log.info("    Valid To   : {}", notAfter);

			Date currentDate = new Date();
			assertTrue(notBefore.getDate().before(currentDate), "Certificate should be active");
			assertTrue(notAfter.getDate().after(currentDate), "Certificate should not be expired");

			// 6. 주체 (Subject) 확인
			X500Name subject = tbsCert.getSubject();
			log.info("[5] Subject: {}", subject.toString());
			assertTrue(subject.toString().contains("Apple Certification Authority"), "Expected Apple Root CA as Subject");

			// 7. 공개 키 정보 확인
			SubjectPublicKeyInfo publicKeyInfo = tbsCert.getSubjectPublicKeyInfo();
			String publicKeyAlgorithmOID = publicKeyInfo.getAlgorithm().getAlgorithm().getId();
			log.info("[6] SubjectPublicKeyInfo");
			log.info("    Public Key Algorithm OID: {}", publicKeyAlgorithmOID);
			assertEquals("1.2.840.113549.1.1.1", publicKeyAlgorithmOID, "Expected RSA Public Key");

			// 8. 확장 필드 (Extensions) 확인
			Extensions extensions = tbsCert.getExtensions();
			if (extensions != null) {
				log.info("[7] Extensions: {}", extensions);

				// Basic Constraints (CA 여부 확인)
				ASN1Encodable basicConstraintsOctet = extensions.getExtensionParsedValue(Extension.basicConstraints);
				if (basicConstraintsOctet != null) {
					BasicConstraints basicConstraints = BasicConstraints.getInstance(basicConstraintsOctet);
					log.info("    Is CA: {}", basicConstraints.isCA());
					assertTrue(basicConstraints.isCA(), "Expected this certificate to be a CA certificate");
				}
			}
		}
	}

	@DisplayName("인증서엔 공개키가 포함되어 있다.")
	@Test
	void getPublicKey() {
		String certDirPath = "appstore/cert/root";
		X509Certificate x509Certificate = loadCertificate(certDirPath, "AppleIncRootCertificate.cer");
		PublicKey publicKey = x509Certificate.getPublicKey();
		String algorithm = publicKey.getAlgorithm();
		log.info("algorithm: {}", algorithm);

		assertThat(publicKey).isNotNull();
	}

	private X509Certificate loadCertificate(String certDirPath, String filename) {
		log.info("========{}========", filename);
		var basedir = "src/test/resources/";
		var filePath = String.format("%s/%s/%s", basedir, certDirPath, filename);
		try (InputStream inputStream = new FileInputStream(filePath)) {
			return CertUtils.loadCertificate(inputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
