package com.gmoon.writeinvalidate.article;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.cacheinvalidation.test.SeparateTransaction;

@IntegrationTest
@DisplayName("@CacheEvict 로 무효화하는 엔티티")
class CacheEvictAnnotationTest {

	private static final String ORIGINAL_TITLE = "cache invalidation";
	private static final String CHANGED_TITLE = "cache invalidation, revised";

	@Autowired ArticleQueryService articleQueryService;
	@Autowired ArticleCommandService articleCommandService;
	@Autowired CacheManager cacheManager;
	@Autowired TransactionTemplate transactionTemplate;

	private Long articleId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(ArticleCachePolicy.Name.ARTICLE).clear();
		articleId = articleCommandService.publish(ORIGINAL_TITLE);
	}

	@Nested
	@DisplayName("쓰기 메서드가 트랜잭션을 직접 소유하면")
	class WhenWriteOwnsTransaction {

		@Test
		@DisplayName("변경 후 첫 조회가 새 값을 반환한다")
		void returnsFreshValueAfterChange() {
			articleQueryService.findById(articleId);

			articleCommandService.changeTitle(articleId, CHANGED_TITLE);

			assertThat(articleQueryService.findById(articleId).title())
				 .as("경합이 없으면 evict 가 커밋 전에 실행되든 후에 실행되든 결과가 같다")
				 .isEqualTo(CHANGED_TITLE);
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션에 참여하면")
	class WhenJoiningOuterTransaction {

		@Test
		@DisplayName("커밋을 기다리지 않고 메서드 반환 직후 캐시를 지운다")
		void evictsBeforeOuterCommit() {
			articleQueryService.findById(articleId);

			transactionTemplate.executeWithoutResult(status -> {
				articleCommandService.changeTitle(articleId, CHANGED_TITLE);

				assertThat(cachedTitle())
					 .as("@CacheEvict 는 자신을 감싼 트랜잭션의 커밋 시점을 알지 못한다")
					 .isNull();
			});
		}

		@Test
		@DisplayName("삭제와 커밋 사이에 끼어든 조회가 옛 값을 다시 적재한다")
		void reinstatesStaleValueReadBeforeCommit() {
			articleQueryService.findById(articleId);

			transactionTemplate.executeWithoutResult(status -> {
				articleCommandService.changeTitle(articleId, CHANGED_TITLE);
				assertThat(cachedTitle())
					 .as("커밋 전에 캐시가 비워져 재적재 창이 열린다")
					 .isNull();

				SeparateTransaction.read(() -> articleQueryService.findById(articleId));
				assertThat(cachedTitle())
					 .as("지워진 자리에 커밋 전 조회가 옛 값을 다시 심는다")
					 .isEqualTo(ORIGINAL_TITLE);
			});

			assertThat(articleQueryService.findById(articleId).title())
				 .as("커밋 전 조회는 옛 값을 보고 그 값을 캐시에 다시 심는다. stale 이 TTL 까지 고착된다")
				 .isEqualTo(ORIGINAL_TITLE);
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션이 롤백되면")
	class WhenOuterTransactionRolledBack {

		@Test
		@DisplayName("이미 지워진 캐시는 되돌아오지 않는다")
		void keepsCacheEvictedAfterRollback() {
			articleQueryService.findById(articleId);

			transactionTemplate.executeWithoutResult(status -> {
				articleCommandService.changeTitle(articleId, CHANGED_TITLE);
				status.setRollbackOnly();
			});

			assertThat(cachedTitle())
				 .as("evict 는 트랜잭션 자원이 아니므로 롤백 대상이 아니다")
				 .isNull();
		}

		@Test
		@DisplayName("캐시를 지웠어도 DB 원본과 어긋나지 않는다")
		void staysConsistentWithDatabaseAfterRollback() {
			articleQueryService.findById(articleId);

			transactionTemplate.executeWithoutResult(status -> {
				articleCommandService.changeTitle(articleId, CHANGED_TITLE);
				status.setRollbackOnly();
			});

			assertThat(articleQueryService.findById(articleId).title())
				 .as("불필요한 미스 1회를 잃을 뿐 정합성은 깨지지 않는다")
				 .isEqualTo(ORIGINAL_TITLE);
		}
	}

	private String cachedTitle() {
		CachedArticle cached = cacheManager.getCache(ArticleCachePolicy.Name.ARTICLE)
			 .get(articleId, CachedArticle.class);
		return cached == null ? null : cached.title();
	}
}
