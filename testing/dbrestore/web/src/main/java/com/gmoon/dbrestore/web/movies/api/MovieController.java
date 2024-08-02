package com.gmoon.dbrestore.web.movies.api;

import com.gmoon.dbrestore.web.movies.application.MovieService;
import com.gmoon.dbrestore.web.movies.domain.Movie;
import com.gmoon.dbrestore.web.movies.domain.TicketOffice;
import com.gmoon.dbrestore.web.movies.dto.CouponRequestVO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@PostMapping("/coupon")
	@ResponseBody
	public HttpEntity<String> issueCoupon(@RequestBody CouponRequestVO requestVO) {
		Long officeId = requestVO.getOfficeId();
		Long movieId = requestVO.getMovieId();

		movieService.issueCoupon(officeId, movieId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/error")
	@ResponseBody
	public HttpEntity<String> error(@RequestBody CouponRequestVO requestVO) {
		Long officeId = requestVO.getOfficeId();
		Long movieId = requestVO.getMovieId();

		Movie movie = movieService.getMovie(officeId, movieId)
			 .orElseThrow(EntityNotFoundException::new);

		// throw LazyInitializationException
		movie.getTickets().size();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping
	@ResponseBody
	public HttpEntity<Void> delete(Long movieId) {
		movieService.removeMovie(movieId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
