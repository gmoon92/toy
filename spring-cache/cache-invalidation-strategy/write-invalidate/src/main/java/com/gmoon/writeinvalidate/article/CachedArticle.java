package com.gmoon.writeinvalidate.article;

public record CachedArticle(Long id, String title) {

	public static CachedArticle from(Article article) {
		return new CachedArticle(article.getId(), article.getTitle());
	}
}
