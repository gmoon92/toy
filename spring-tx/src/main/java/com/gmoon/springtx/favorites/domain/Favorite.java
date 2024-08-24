package com.gmoon.springtx.favorites.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_favorite")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Favorite implements Serializable {

	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@OneToMany(
		 mappedBy = "favorite",
		 fetch = FetchType.LAZY,
		 cascade = CascadeType.REMOVE,
		 orphanRemoval = true
	)
	private Set<FavoriteUser> favoriteUsers = new HashSet<>();

	public Favorite(String userId, FavoriteType type) {
		id = new Id(userId, type);
	}

	@Embeddable
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	protected static class Id implements Serializable {

		@Column(name = "user_id", length = 50, nullable = false, updatable = false)
		@EqualsAndHashCode.Include
		private String userId;

		@Enumerated(EnumType.STRING)
		@Column(length = 50, nullable = false, updatable = false)
		private FavoriteType type;

		Id(String userId, FavoriteType type) {
			this.userId = userId;
			this.type = type;
		}
	}
}
