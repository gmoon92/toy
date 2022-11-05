package com.gmoon.springjpaspecs.books.bookstore.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springjpaspecs.books.bookstore.application.BookStoreService;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentRequest;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookstore")
@RequiredArgsConstructor
public class BookStoreRestController {

	private final BookStoreService bookStoreService;

	@GetMapping
	public ResponseEntity<List<BookStoreContentResponse>> findAll(@RequestBody BookStoreContentRequest request) {
		return ResponseEntity.ok(bookStoreService.findAll(request));
	}
}
