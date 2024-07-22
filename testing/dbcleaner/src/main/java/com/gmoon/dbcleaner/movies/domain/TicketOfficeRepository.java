package com.gmoon.dbcleaner.movies.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TicketOfficeRepository {

	Optional<TicketOffice> findById(Long id);

	List<TicketOffice> findAll();

	Page<TicketOffice> findAllByMovieId(Long movieId, Pageable pageable);
}
