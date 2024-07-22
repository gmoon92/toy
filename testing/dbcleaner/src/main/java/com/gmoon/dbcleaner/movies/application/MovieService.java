package com.gmoon.dbcleaner.movies.application;

import com.gmoon.dbcleaner.movies.domain.TicketOffice;
import com.gmoon.dbcleaner.movies.domain.TicketOfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieService {

	private final TicketOfficeRepository ticketOfficeRepository;

	@Transactional(readOnly = true)
	public Page<TicketOffice> getTickerOffices(Long movieId, Pageable pageable) {
		return ticketOfficeRepository.findAllByMovieId(movieId, pageable);
	}
}
