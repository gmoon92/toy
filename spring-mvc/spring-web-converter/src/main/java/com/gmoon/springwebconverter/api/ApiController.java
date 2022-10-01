package com.gmoon.springwebconverter.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springwebconverter.model.SearchType;

@RestController
@RequestMapping("/api")
public class ApiController {

	@GetMapping
	public ResponseEntity<SearchType> searchType(SearchType searchType) {
		return ResponseEntity.ok(searchType);
	}
}
