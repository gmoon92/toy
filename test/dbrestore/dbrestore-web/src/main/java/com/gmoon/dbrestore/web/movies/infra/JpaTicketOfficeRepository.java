package com.gmoon.dbrestore.web.movies.infra;

import com.gmoon.dbrestore.web.movies.domain.Movie;
import com.gmoon.dbrestore.web.movies.domain.TicketOffice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaTicketOfficeRepository extends JpaRepository<TicketOffice, Long> {

	/**
	 * @link {https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.at-query.native}
	 */
	@Query("SELECT to, m FROM TicketOffice to JOIN to.movies m WHERE m.id = :movieId")
	Page<TicketOffice> findAllByMovieIdAndPage(@Param("movieId") Long movieId, Pageable pageable);

	// @Query("SELECT m FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
	@Query("FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
	Optional<Movie> findByIdAndMovieId(Long id, Long movieId);
}
