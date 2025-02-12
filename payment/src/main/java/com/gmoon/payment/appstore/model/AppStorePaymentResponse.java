package com.gmoon.payment.appstore.model;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.gmoon.payment.global.utils.NumberUtils;

public record AppStorePaymentResponse(
	 String transactionId,
	 String orgTransactionId,
	 double price,
	 Long purchaseTime
) {

	/**
	 * The price, in milliunits, of the in-app purchase or subscription offer that you configured in App Store Connect.
	 *
	 * @return price
	 * @see <a href="https://developer.apple.com/documentation/appstoreserverapi/price">price</a>
	 **/
	public static AppStorePaymentResponse from(JWSTransactionDecodedPayload payload) {
		double price = NumberUtils.toBigDecimal(payload.getPrice())
			 .multiply(NumberUtils.toBigDecimal(payload.getQuantity()))
			 .divide(NumberUtils.toBigDecimal(1_000))
			 .doubleValue();

		return new AppStorePaymentResponse(
			 payload.getTransactionId(),
			 payload.getOriginalTransactionId(),
			 price,
			 payload.getPurchaseDate()
		);
	}
}
