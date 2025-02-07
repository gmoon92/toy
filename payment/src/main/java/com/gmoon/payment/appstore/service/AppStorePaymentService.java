package com.gmoon.payment.appstore.service;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.gmoon.payment.appstore.infra.AppStoreClient;
import com.gmoon.payment.appstore.model.AppStorePaymentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppStorePaymentService {

	@Nullable
	private final AppStoreClient client;

	public AppStorePaymentResponse getTransactionInfo(String transactionId) {
		var payload = client.verifyAndDecodeTransaction(transactionId);

		return AppStorePaymentResponse.from(payload);
	}
}
