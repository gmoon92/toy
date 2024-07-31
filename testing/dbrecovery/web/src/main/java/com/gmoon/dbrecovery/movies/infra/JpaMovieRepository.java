package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMovieRepository extends JpaRepository<Movie, Long> {

}
