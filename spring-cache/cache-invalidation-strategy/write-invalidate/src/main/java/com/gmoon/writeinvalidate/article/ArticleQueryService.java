package com.gmoon.writeinvalidate.article;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleQueryService {

	private final ArticleRepository articleRepository;

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = ArticleCachePolicy.Name.ARTICLE, key = "#id")
	public CachedArticle findById(Long id) {
		return articleRepository.findById(id)
			 .map(CachedArticle::from)
			 .orElse(null);
	}
}
