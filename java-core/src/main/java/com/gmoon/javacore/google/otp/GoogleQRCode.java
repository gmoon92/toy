package com.gmoon.javacore.google.otp;

import java.io.ByteArrayOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleQRCode {

	public static byte[] createImageBytes(String contents) {
		BitMatrix bitMatrix = newBitMatrix(contents);
		return createImageOutputStream(bitMatrix)
			.toByteArray();
	}

	private static BitMatrix newBitMatrix(String contents) {
		int width = 200;
		int height = 200;
		try {
			QRCodeWriter writer = new QRCodeWriter();
			return writer.encode(contents, BarcodeFormat.QR_CODE, width, height);
		} catch (WriterException e) {
			throw new RuntimeException(e);
		}
	}

	private static ByteArrayOutputStream createImageOutputStream(BitMatrix matrix) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return out;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
