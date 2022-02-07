package com.gmoon.resourceserver.cors;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.resourceserver.util.CorsUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CorsOriginService {
	private final CorsOriginRepository repository;
	private final CorsUtils utils;

	@Transactional(readOnly = true)
	@Cacheable(value = "corsConfig", key = "#root.methodName")
	public List<String> getAllowedOriginPatterns() {
		List<String> hosts = repository.getAllHost();
		return utils.getAllowedOriginPatterns(hosts);
	}

	@Transactional(readOnly = true)
	public List<CorsOrigin> getAll() {
		return repository.findAll();
	}

	@Transactional
	public CorsOrigin save(CorsOrigin corsOrigin) {
		return repository.save(corsOrigin);
	}

	@Transactional
	public void delete(Long id) {
		repository.findById(id)
				.ifPresent(repository::delete);
	}
}
