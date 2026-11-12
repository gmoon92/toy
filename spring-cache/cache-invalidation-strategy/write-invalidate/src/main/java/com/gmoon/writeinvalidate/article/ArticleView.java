package com.gmoon.writeinvalidate.article;

public record ArticleView(Long id, String title) {

	public static ArticleView from(Article article) {
		return new ArticleView(article.getId(), article.getTitle());
	}
}
