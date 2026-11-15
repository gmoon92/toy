package com.gmoon.outboxinvalidate.cache;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.gmoon.outboxinvalidate.config.CachePolicy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커밋됐지만 아직 브로커로 보내지 않은 무효화 지시. outbox 테이블의 한 행이다.
 */
@Entity
@Table(name = "pending_eviction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PendingEviction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CachePolicy policy;

	@Column(name = "cache_key", nullable = false)
	private String key;

	@Column(nullable = false)
	private Instant recordedAt;

	private PendingEviction(CacheKey cacheKey) {
		this.policy = cacheKey.policy();
		this.key = cacheKey.key();
		this.recordedAt = Instant.now();
	}

	public static PendingEviction of(CacheKey cacheKey) {
		return new PendingEviction(cacheKey);
	}

	public CacheKey cacheKey() {
		return new CacheKey(policy, key);
	}
}
