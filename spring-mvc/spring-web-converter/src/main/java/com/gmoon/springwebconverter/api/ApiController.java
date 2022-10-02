package com.gmoon.springwebconverter.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springwebconverter.model.PaymentType;
import com.gmoon.springwebconverter.model.SearchType;

@RestController
@RequestMapping("/api")
public class ApiController {

	@GetMapping("/searchType")
	public ResponseEntity<SearchType> searchType(SearchType searchType) {
		return ResponseEntity.ok(searchType);
	}

	@GetMapping("/paymentType")
	public ResponseEntity<PaymentType> paymentType(PaymentType paymentType) {
		return ResponseEntity.ok(paymentType);
	}
}
