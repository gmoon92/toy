package com.gmoon.payment.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.security.cert.*;
import java.time.Instant;
import java.util.Date;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CertUtils {

	/**
	 * @param type the name of the requested certificate type.
	 *             See the CertificateFactory section in the <a href="https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#certificatefactory-types">Java Security Standard Algorithm Names Specification</a>
	 *             for information about standard certificate types.
	 *             The certificate type defined in X.509, also specified in <a href="https://datatracker.ietf.org/doc/html/rfc5280">RFC 5280.</a>
	 */
	private static CertificateFactory newCertFactory(String type) throws CertificateException {
		return CertificateFactory.getInstance(type);
	}

	public static X509Certificate loadCertificate(InputStream certInputStream) {
		try {
			CertificateFactory factory = newCertFactory("X.509");
			Certificate certificate = factory.generateCertificate(certInputStream);
			if (certificate instanceof X509Certificate x509Certificate) {
				return x509Certificate;
			}

			throw new RuntimeException("Root certificate not of the expected X509 format");
		} catch (CertificateException e) {
			throw new RuntimeException("Invalid certificate file.", e);
		}
	}

	public static boolean isCertificateValid(X509Certificate certificate, Instant instant) {
		try {
			certificate.checkValidity(Date.from(instant));
			return true;
		} catch (CertificateExpiredException | CertificateNotYetValidException e) {
			log.warn("Certificate is not valid: {}", e.getMessage());
			return false;
		}
	}

	public static boolean isCertificateValid(X509Certificate certificate) {
		return isCertificateValid(certificate, Instant.now());
	}
}
