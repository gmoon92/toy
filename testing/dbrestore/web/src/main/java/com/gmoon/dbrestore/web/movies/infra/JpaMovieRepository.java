package com.gmoon.dbrestore.web.movies.infra;

import com.gmoon.dbrestore.web.movies.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMovieRepository extends JpaRepository<Movie, Long> {

}
