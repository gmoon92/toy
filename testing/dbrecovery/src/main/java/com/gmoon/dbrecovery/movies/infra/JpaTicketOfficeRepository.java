package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.Movie;
import com.gmoon.dbrecovery.movies.domain.TicketOffice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTicketOfficeRepository extends JpaRepository<TicketOffice, Long> {

	/**
	 * @link {https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.at-query.native}
	 */
	@Query("SELECT to, m FROM TicketOffice to JOIN to.movies m WHERE m.id = :movieId")
	Page<TicketOffice> findAllByMovieIdAndPage(@Param("movieId") Long movieId, Pageable pageable);

	@Query("SELECT m FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
	Optional<Movie> findByIdAndMovieId(Long id, Long movieId);
}