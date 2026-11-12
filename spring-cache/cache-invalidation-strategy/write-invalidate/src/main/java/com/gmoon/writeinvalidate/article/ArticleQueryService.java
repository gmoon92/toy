package com.gmoon.writeinvalidate.article;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.writeinvalidate.config.CachePolicy;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleQueryService {

	private final ArticleRepository articleRepository;

	@Cacheable(cacheNames = CachePolicy.Name.ARTICLE, key = "#id")
	public ArticleView findById(Long id) {
		return articleRepository.findById(id)
			 .map(ArticleView::from)
			 .orElse(null);
	}
}
