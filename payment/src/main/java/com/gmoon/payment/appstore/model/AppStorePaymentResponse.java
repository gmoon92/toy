package com.gmoon.payment.appstore.model;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;

public record AppStorePaymentResponse(
	 String transactionId,
	 String orgTransactionId,
	 Long price,
	 Long purchaseTime
) {

	public static AppStorePaymentResponse from(JWSTransactionDecodedPayload payload) {
		return new AppStorePaymentResponse(
			 payload.getTransactionId(),
			 payload.getOriginalTransactionId(),
			 payload.getPrice(),
			 payload.getPurchaseDate()
		);
	}
}
