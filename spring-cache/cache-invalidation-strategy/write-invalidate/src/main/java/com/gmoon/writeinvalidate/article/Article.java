package com.gmoon.writeinvalidate.article;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private long viewCount;

	private Article(String title) {
		this.title = title;
		this.viewCount = 0L;
	}

	public static Article of(String title) {
		return new Article(title);
	}

	public void changeTitle(String title) {
		this.title = title;
	}

	public void increaseViewCount() {
		this.viewCount++;
	}
}
