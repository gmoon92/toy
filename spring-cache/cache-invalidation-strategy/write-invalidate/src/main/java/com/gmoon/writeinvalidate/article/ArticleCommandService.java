package com.gmoon.writeinvalidate.article;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleCommandService {

	private final ArticleRepository articleRepository;

	@Transactional
	public Long publish(String title) {
		return articleRepository.save(Article.of(title)).getId();
	}

	@Transactional
	@CacheEvict(cacheNames = ArticleCachePolicy.Name.ARTICLE, key = "#id")
	public void changeTitle(Long id, String title) {
		findArticle(id).changeTitle(title);
	}

	@Transactional
	public void increaseViewCount(Long id) {
		findArticle(id).increaseViewCount();
	}

	private Article findArticle(Long id) {
		return articleRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
	}
}
