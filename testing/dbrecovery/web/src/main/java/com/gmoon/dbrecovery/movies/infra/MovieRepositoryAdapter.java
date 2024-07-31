package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.MovieRepository;
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
