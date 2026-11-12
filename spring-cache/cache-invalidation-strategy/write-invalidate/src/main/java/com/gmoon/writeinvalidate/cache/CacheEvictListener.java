package com.gmoon.writeinvalidate.cache;

import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.writeinvalidate.config.JpaEntityEventListener;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * 엔티티가 바뀌었다는 신호를 받아, {@link CacheEvictable} 이면 그 키를 커밋 이후에 지운다.
 * <p>
 * 신호원은 둘이다. 영속성 컨텍스트를 거친 변경은 하이버네이트 엔티티 이벤트로 받고,
 * JPA 를 거치지 않은 쓰기(JDBC, 벌크 연산, 외부 연동)는 직접 발행한 엔티티로 받는다.
 * 벌크 연산은 발행하지 않으면 무효화도 누락된다.
 * <p>
 * 어느 신호원으로 와도 커밋 이후에 지운다. 같은 변경을 두 신호원으로 알리면 {@code DEL} 도 두 번 나가지만,
 * 몇 번 지워도 결과는 같으므로 걸러내지 않는다.
 */
@Slf4j
public class CacheEvictListener extends JpaEntityEventListener<CacheEvictable> {

	private final CacheEvictMetrics metrics;
	private final AfterCommitEvictor evictor;

	public CacheEvictListener(CacheManager cacheManager, @Nullable MeterRegistry meterRegistry) {
		this.metrics = new CacheEvictMetrics(meterRegistry);
		this.evictor = new AfterCommitEvictor(cacheManager, metrics);
	}

	/**
	 * 무효화 실패는 호출자의 데이터 변경을 되돌리지 않는다.
	 * 신호는 flush 중이거나 커밋 직전, 즉 트랜잭션 안에서 도착하므로
	 * 여기서 예외가 새어나가면 캐시 때문에 트랜잭션이 롤백된다.
	 */
	@Override
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
	public void onEntityChanged(CacheEvictable entity) {
		try {
			evictor.evict(cacheKeysOf(entity));
		} catch (RuntimeException e) {
			log.warn("cache evict failed. the caller's transaction is not affected", e);
		}
	}

	private Set<CacheKey> cacheKeysOf(CacheEvictable entity) {
		try {
			return entity.cacheKeys();
		} catch (RuntimeException e) {
			String entityName = entity.getClass().getSimpleName();
			log.warn("failed to read cache keys of the changed entity. entity: {}", entityName, e);
			metrics.resolutionFailed(entityName);
			return Set.of();
		}
	}
}
