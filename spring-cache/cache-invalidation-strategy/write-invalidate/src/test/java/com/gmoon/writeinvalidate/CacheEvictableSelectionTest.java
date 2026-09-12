package com.gmoon.writeinvalidate;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.cacheinvalidation.core.metrics.DatabaseQueryCounter;
import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.writeinvalidate.article.ArticleCachePolicy;
import com.gmoon.writeinvalidate.article.ArticleCommandService;
import com.gmoon.writeinvalidate.article.ArticleQueryService;
import com.gmoon.writeinvalidate.article.CachedArticle;
import com.gmoon.writeinvalidate.user.CachedUser;
import com.gmoon.writeinvalidate.user.UserCachePolicy;
import com.gmoon.writeinvalidate.user.UserCommandService;
import com.gmoon.writeinvalidate.user.UserQueryService;

@IntegrationTest
@DisplayName("엔티티 이벤트 리스너의 대상 선별")
class CacheEvictableSelectionTest {

	private static final String ORIGINAL_TITLE = "cache invalidation";
	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired ArticleQueryService articleQueryService;
	@Autowired ArticleCommandService articleCommandService;
	@Autowired CacheManager cacheManager;
	@Autowired TransactionTemplate transactionTemplate;
	@Autowired DatabaseQueryCounter databaseQueryCounter;

	private Long userId;
	private Long articleId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(UserCachePolicy.Name.USER).clear();
		cacheManager.getCache(ArticleCachePolicy.Name.ARTICLE).clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
		articleId = articleCommandService.publish(ORIGINAL_TITLE);
	}

	@Nested
	@DisplayName("하나의 커밋에 두 종류의 엔티티가 섞여 있으면")
	class WhenCommitMixesEntityTypes {

		@Test
		@DisplayName("CacheEvictable 구현체의 캐시만 지운다")
		void evictsOnlyCacheEvictableEntities() {
			userQueryService.findById(userId);
			articleQueryService.findById(articleId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				articleCommandService.increaseViewCount(articleId);
			});

			assertSoftly(softly -> {
				softly.assertThat(cachedEmail())
					 .as("User 는 CacheEvictable 이므로 무효화된다")
					 .isNull();
				softly.assertThat(cachedTitle())
					 .as("Article 은 CacheEvictable 이 아니므로 리스너가 건너뛴다")
					 .isEqualTo(ORIGINAL_TITLE);
			});
		}
	}

	@Nested
	@DisplayName("리스너가 무효화를 수행할 때")
	class WhenListenerInvalidates {

		@Test
		@DisplayName("무효화 대상 키를 얻으려고 DB 를 다시 읽지 않는다")
		void resolvesKeysWithoutQueryingDatabase() {
			databaseQueryCounter.reset();
			articleCommandService.increaseViewCount(articleId);
			long withoutEviction = databaseQueryCounter.executedStatementCount();

			databaseQueryCounter.reset();
			userCommandService.changeEmail(userId, CHANGED_EMAIL);
			long withEviction = databaseQueryCounter.executedStatementCount();

			assertThat(withEviction)
				 .as("이벤트의 목적은 캐시 삭제뿐이다. 키는 이미 로딩된 엔티티에서 나온다")
				 .isEqualTo(withoutEviction);
		}
	}

	private String cachedEmail() {
		CachedUser cached = cacheManager.getCache(UserCachePolicy.Name.USER)
			 .get(userId, CachedUser.class);
		return cached == null ? null : cached.email();
	}

	private String cachedTitle() {
		CachedArticle cached = cacheManager.getCache(ArticleCachePolicy.Name.ARTICLE)
			 .get(articleId, CachedArticle.class);
		return cached == null ? null : cached.title();
	}
}
