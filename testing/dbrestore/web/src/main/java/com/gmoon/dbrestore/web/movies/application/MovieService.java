package com.gmoon.dbrestore.web.movies.application;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.dbrestore.web.logs.domain.IssueCoupon;
import com.gmoon.dbrestore.web.movies.domain.Coupon;
import com.gmoon.dbrestore.web.movies.domain.CouponRepository;
import com.gmoon.dbrestore.web.movies.domain.Movie;
import com.gmoon.dbrestore.web.movies.domain.MovieRepository;
import com.gmoon.dbrestore.web.movies.domain.Ticket;
import com.gmoon.dbrestore.web.movies.domain.TicketOffice;
import com.gmoon.dbrestore.web.movies.domain.TicketOfficeRepository;
import com.gmoon.dbrestore.web.movies.domain.vo.TicketType;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MovieService {

	private final TicketOfficeRepository ticketOfficeRepository;
	private final CouponRepository couponRepository;
	private final MovieRepository movieRepository;
	private final ApplicationEventPublisher publisher;

	public Page<TicketOffice> getTickerOffices(Long movieId, Pageable pageable) {
		return ticketOfficeRepository.findAllByMovieId(movieId, pageable);
	}

	public Optional<Movie> getMovie(Long officeId, Long movieId) {
		return ticketOfficeRepository.findMovie(officeId, movieId);
	}

	public List<Coupon> getCoupons(Long movieId) {
		return couponRepository.findAllByMovieId(movieId);
	}

	@Transactional
	public void issueCoupon(Long officeId, Long movieId) {
		Movie movie = getMovie(officeId, movieId)
			 .orElseThrow(EntityNotFoundException::new);

		Coupon coupon = getCoupons(movie.getId())
			 .stream()
			 .filter(c -> !c.isUsed())
			 .findFirst()
			 .orElseThrow(() -> new IllegalArgumentException("Not issued coupon."));

		TicketOffice ticketOffice = movie.getTicketOffice();
		Ticket ticket = ticketOffice.createTicket(TicketType.FREE, 0);
		movie.addTicket(ticket);

		coupon.using();

		log.info("New coupon added: {}", coupon);
		publisher.publishEvent(new IssueCoupon(coupon));
	}

	@Transactional
	public void removeMovie(Long movieId) {
		movieRepository.remove(movieId);
	}
}
