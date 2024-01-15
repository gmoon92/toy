package com.gmoon.javacore.google.otp;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class GoogleQRCodeTest {

	@DisplayName("https://github.com/zxing/zxing")
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
}
