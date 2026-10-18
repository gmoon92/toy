package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("캐시 정책 선언")
class CachePolicyTest {

	@Nested
	@DisplayName("서로 다른 정책이 같은 캐시 이름을 선언하면")
	class WhenCacheNameDuplicated {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatIllegalArgumentException()
				 .as("허용하면 어느 쪽 TTL 과 값 타입이 이겼는지 런타임에만 드러난다")
				 .isThrownBy(() -> CachePolicy.distinct(List.of(
					  OnePolicy.SHARED,
					  OtherPolicy.SHARED
				 )))
				 .withMessageContaining("Duplicated cache name");
		}
	}

	@Nested
	@DisplayName("enum 상수 이름과 캐시 이름이 다르면")
	class WhenCacheNameDiffersFromConstant {

		@Test
		@DisplayName("그대로 등록한다")
		void registersAsDeclared() {
			assertThat(CachePolicy.distinct(List.of(RenamedPolicy.USER)))
				 .as("삭제 키와 @Cacheable 은 모두 Spec 의 캐시명을 쓴다. 상수 이름은 어떤 키에도 쓰이지 않는다")
				 .containsExactly(RenamedPolicy.USER);
		}
	}

	@Nested
	@DisplayName("정상적인 정책이면")
	class WhenPolicyWellFormed {

		@Test
		@DisplayName("선언 순서를 지켜 돌려준다")
		void keepsDeclarationOrder() {
			assertThat(CachePolicy.distinct(List.of(TestCachePolicy.values())))
				 .containsExactly(
					  TestCachePolicy.USER,
					  TestCachePolicy.USER_SUMMARY
				 );
		}
	}

	private enum RenamedPolicy implements CachePolicy {

		USER(new Spec("user:v2", Duration.ofMinutes(1), String.class));

		private final Spec spec;

		RenamedPolicy(Spec spec) {
			this.spec = spec;
		}

		@Override
		public Spec spec() {
			return spec;
		}
	}

	private enum OnePolicy implements CachePolicy {

		SHARED(new Spec("SHARED", Duration.ofMinutes(1), String.class));

		private final Spec spec;

		OnePolicy(Spec spec) {
			this.spec = spec;
		}

		@Override
		public Spec spec() {
			return spec;
		}
	}

	private enum OtherPolicy implements CachePolicy {

		SHARED(new Spec("SHARED", Duration.ofMinutes(2), String.class));

		private final Spec spec;

		OtherPolicy(Spec spec) {
			this.spec = spec;
		}

		@Override
		public Spec spec() {
			return spec;
		}
	}
}
