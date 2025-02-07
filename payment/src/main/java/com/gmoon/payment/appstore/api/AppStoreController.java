package com.gmoon.payment.appstore.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.payment.appstore.model.AppStorePaymentResponse;
import com.gmoon.payment.appstore.service.AppStorePaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/appstore")
@RestController
@RequiredArgsConstructor
public class AppStoreController {

	private final AppStorePaymentService appStorePaymentService;

	@GetMapping("/{transactionId}")
	public HttpEntity<AppStorePaymentResponse> getTransactionInfo(@PathVariable String transactionId) {
		log.debug("appstore receipt transaction id: {}", transactionId);
		return ResponseEntity.ok(appStorePaymentService.getTransactionInfo(transactionId));
	}
}
