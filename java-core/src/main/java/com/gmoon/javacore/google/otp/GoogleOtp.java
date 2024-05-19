package com.gmoon.javacore.google.otp;

import java.time.Duration;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.Getter;

/**
 * @see {https://2fasolution.com/}
 */
@Getter
public class GoogleOtp {

	private static final HashingAlgorithm DEFAULT_ALGORITHM = HashingAlgorithm.SHA256;
	private final HashingAlgorithm algorithm;
	private final int digits;
	private final Duration period;
	private final String secret;

	public GoogleOtp() {
		SecretGenerator generator = new DefaultSecretGenerator();
		this.secret = generator.generate();
		this.algorithm = DEFAULT_ALGORITHM;
		this.digits = 6;
		this.period = Duration.ofSeconds(30);
	}

	public GoogleOtp(String secret) {
		this.secret = secret;
		this.algorithm = DEFAULT_ALGORITHM;
		this.digits = 6;
		this.period = Duration.ofSeconds(30);
	}

	/**
	 * @see QrData#getUri() otpauth://...
	 * */
	public String getImageDataUrl() {
		QrData data = new QrData.Builder()
			 .label("gmoon-2FA")
			 .secret(secret)
			 .algorithm(algorithm)
			 .digits(digits)
			 .period((int)period.getSeconds())
			 .build();
		return QRCodeImage.getImageDataUrl(data.getUri());
	}

	public boolean verify(String otp) {
		CodeGenerator codeGenerator = new DefaultCodeGenerator(algorithm, digits);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
		return verifier.isValidCode(secret, otp);
	}
}
