package com.gmoon.javacore.google.otp;

import static org.assertj.core.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class GoogleQRCodeTest {

	@DisplayName("참고 https://github.com/zxing/zxing")
	@Test
	void createQRCodeImageFile() {
		String filepath = "src/test/resources/google/qrcode.png";
		try (FileOutputStream fos = new FileOutputStream(filepath)){
			String contents = "https://github.com/gmoon92";
			fos.write(GoogleQRCode.createImageBytes(contents));
			log.info("Created QR code image file.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@DisplayName("https://github.com/samdjstevens/java-totp")
	@Test
	void getImageDataUrl() {
		String contents = "https://github.com/gmoon92";

		String imageUrl = GoogleQRCode.getImageDataUrl(contents);

		assertThat(imageUrl).isEqualTo(
			"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAABTElEQVR42u2YPbKDMAyElaGg5AgchaPho3EUjuAyRcaK/pxkGPPq5x2rSWy+xmshrSD+Ow4awAAGMID/DjxJY3ou8r8QLaetFzjAnihw0ENW51Y3wQA5uQC0cioT87mlUAUToC25DsgAZwEsq1EBz2rRwfG7tO8cqDVKBNB3+L6IdQ184qS9xD23W1LXgBUn8uLk2/tVKAhALzgO7k/S4zUzGqACMFmNYoqYGA6oOuRorHLdhWY4wFqNnVzWrzkTXbIaA7AaHD8igMrB17QHADyrzdTv6h9ku5X2/QN2z6T+wbzvQZeshgC047C6wfXHTTAcEGGTmvuH1DQYnQP15PlrCiGBOnd/3GC6+bzQOeADqQLttEcC1NtrcdLGWlo6gAB5FVPvc2nDFQMAMXe7G7R3mACBmLu1o6o/EjdRmoN538D4LD+AAQwAFngD3MQ50TXLstcAAAAASUVORK5CYII="
		);
	}
}
