package com.gmoon.dbrecovery.movies.application;

import com.gmoon.dbrecovery.movies.domain.Coupon;
import com.gmoon.dbrecovery.movies.domain.CouponRepository;
import com.gmoon.dbrecovery.movies.domain.Movie;
import com.gmoon.dbrecovery.movies.domain.Ticket;
import com.gmoon.dbrecovery.movies.domain.TicketOffice;
import com.gmoon.dbrecovery.movies.domain.TicketOfficeRepository;
import com.gmoon.dbrecovery.movies.domain.vo.TicketType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {

	private final TicketOfficeRepository ticketOfficeRepository;
	private final CouponRepository couponRepository;

	@Transactional(readOnly = true)
	public Page<TicketOffice> getTickerOffices(Long movieId, Pageable pageable) {
		return ticketOfficeRepository.findAllByMovieId(movieId, pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Movie> getMovie(Long officeId, Long movieId) {
		return ticketOfficeRepository.findMovie(officeId, movieId);
	}

	@Transactional(readOnly = true)
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
	}
}
