package com.gmoon.writeinvalidate.cache;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.CacheKey;
import com.gmoon.cacheinvalidation.EntityChange;
import com.gmoon.writeinvalidate.user.UserCachePolicy;

@DisplayName("엔티티가 선언한 캐시 무효화 규칙")
class EvictableEntityRuleTest {

	private final EvictableEntityRule rule = EvictableEntityRule.owning(UserCachePolicy.USER);

	@Nested
	@DisplayName("엔티티가 CacheEvictable 을 구현하면")
	class WhenEntityDeclaresItsOwnEntries {

		@Test
		@DisplayName("엔티티가 선언한 캐시를 모두 대상으로 삼는다")
		void resolvesEveryDeclaredEntry() {
			EntityChange change = EntityChange.committed(
				 new EvictableUser(1L), EntityChange.Type.UPDATE, null);

			assertThat(rule.supports(change)).isTrue();
			assertThat(rule.resolve(change))
				 .as("엔티티가 자기 키를 안다는 전제가 깨지면 무효화 대상이 비어 stale 이 남는다")
				 .containsExactly(CacheKey.of(UserCachePolicy.USER, 1L));
		}
	}

	@Nested
	@DisplayName("엔티티가 CacheEvictable 을 구현하지 않으면")
	class WhenEntityIsNotEvictable {

		@Test
		@DisplayName("같은 커밋에 섞여 있어도 반응하지 않는다")
		void doesNotSupport() {
			EntityChange change = EntityChange.committed(new Object(), EntityChange.Type.UPDATE, null);

			assertThat(rule.supports(change))
				 .as("선언하지 않은 엔티티까지 반응하면 남의 캐시를 지운다")
				 .isFalse();
		}
	}

	private record EvictableUser(Long id) implements CacheEvictable {

		@Override
		public List<CacheKey> cacheEntriesToEvict() {
			return List.of(CacheKey.of(UserCachePolicy.USER, id));
		}
	}
}
