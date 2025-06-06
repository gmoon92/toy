package com.gmoon.dbrestore.web.movies.infra;

import com.gmoon.dbrestore.web.movies.domain.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepository {

	private final JpaMovieRepository repository;

	@Override
	public void remove(Long id) {
		repository.deleteById(id);
	}
}
