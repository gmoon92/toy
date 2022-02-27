package com.gmoon.resourceserver.cors;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.resourceserver.constants.CacheNames;
import com.gmoon.resourceserver.util.CorsUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CorsOriginService {
	private final CorsOriginRepository corsOriginRepository;
	private final CorsHttpMethodRepository corsHttpMethodRepository;

	private final CorsUtils corsUtils;

	@Transactional(readOnly = true)
	@Cacheable(value = CacheNames.CORS_CONFIG, key = "#root.methodName")
	public List<String> getAllowedOriginPatterns() {
		List<String> hosts = corsOriginRepository.getAllHost();
		return corsUtils.getAllowedOriginPatterns(hosts);
	}

	@Transactional(readOnly = true)
	public List<CorsOrigin> getAll() {
		return corsOriginRepository.findAll();
	}

	@Transactional
	public CorsOrigin save(CorsOrigin corsOrigin) {
		return corsOriginRepository.save(corsOrigin);
	}

	@Transactional
	public void delete(Long id) {
		corsOriginRepository.findById(id)
				.ifPresent(corsOriginRepository::delete);
	}

	@Transactional
	public List<String> getAllowedHttpMethods() {
		return corsHttpMethodRepository.findAllByEnabled();
	}
}
