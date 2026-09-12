package com.gmoon.writeinvalidate.user;

import java.util.List;

import com.gmoon.cacheinvalidation.core.cache.CacheEntryRef;
import com.gmoon.cacheinvalidation.core.cache.CacheEvictable;

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
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements CacheEvictable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String email;

	private User(String username, String email) {
		this.username = username;
		this.email = email;
	}

	public static User of(String username, String email) {
		return new User(username, email);
	}

	public void changeEmail(String email) {
		this.email = email;
	}

	@Override
	public List<CacheEntryRef> cacheEntriesToEvict() {
		return List.of(CacheEntryRef.of(UserCachePolicy.USER, id));
	}
}
