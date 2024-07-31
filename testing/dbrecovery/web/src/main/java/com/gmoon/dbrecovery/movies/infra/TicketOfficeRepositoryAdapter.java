package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.Movie;
import com.gmoon.dbrecovery.movies.domain.TicketOffice;
import com.gmoon.dbrecovery.movies.domain.TicketOfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketOfficeRepositoryAdapter implements TicketOfficeRepository {

	private final JpaTicketOfficeRepository repository;

	@Override
	public Optional<TicketOffice> findById(Long id) {
		return repository.findById(id);
	}

	@Override
	public List<TicketOffice> findAll() {
		return repository.findAll();
	}

	@Override
	public Page<TicketOffice> findAllByMovieId(Long movieId, Pageable pageable) {
		return repository.findAllByMovieIdAndPage(movieId, pageable);
	}

	@Override
	public Optional<Movie> findMovie(Long id, Long movieId) {
		return repository.findByIdAndMovieId(id, movieId);
	}
}
