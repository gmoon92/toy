package com.gmoon.springtx.favorites.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_favorite")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
public class Favorite implements Serializable {

	@EmbeddedId
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
	@EqualsAndHashCode
	protected static class Id implements Serializable {

		@Column(name = "user_id", length = 50, nullable = false, updatable = false)
		private String userId;

		@Enumerated(EnumType.STRING)
		@Column(name = "type", length = 50, nullable = false, updatable = false)
		private FavoriteType type;

		Id(String userId, FavoriteType type) {
			this.userId = userId;
			this.type = type;
		}
	}
}
