package com.gmoon.dbcleaner.movies.api;

import com.gmoon.dbcleaner.movies.application.MovieService;
import com.gmoon.dbcleaner.movies.domain.TicketOffice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/ticketoffices")
	@ResponseBody
	public HttpEntity<PagedModel<TicketOffice>> findTicketOffices(Long movieId, Pageable pageable) {
		return ResponseEntity.ok(new PagedModel<>(movieService.getTickerOffices(movieId, pageable)));
	}
}
