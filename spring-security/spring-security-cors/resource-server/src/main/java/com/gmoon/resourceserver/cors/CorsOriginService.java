package com.gmoon.resourceserver.cors;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.resourceserver.constants.CacheName;
import com.gmoon.resourceserver.util.CorsUtils;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CorsOriginService {
	private final CorsOriginRepository corsOriginRepository;
	private final CorsHttpMethodRepository corsHttpMethodRepository;

	@Cacheable(value = CacheName.Constants.ALLOWED_ORIGIN_PATTERN, key = "#root.methodName")
	public List<String> getAllowedOriginPatterns() {
		List<String> hosts = corsOriginRepository.getAllHost();
		return getAllowedOriginPatterns(hosts);
	}

	public List<String> getAllowedOriginPatterns(List<String> hosts) {
		return CorsUtils.getAllowedOriginPatterns(hosts);
	}

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

	@Cacheable(value = CacheName.Constants.ALLOWED_HTTP_METHODS, key = "#root.methodName")
	public List<String> getAllowedHttpMethods() {
		return corsHttpMethodRepository.findAllByEnabled();
	}
}
