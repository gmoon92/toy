package com.gmoon.springtx.favorites.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_favorite_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class FavoriteUser implements Serializable {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 50)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
		 @JoinColumn(name = "fuser_id", referencedColumnName = "user_id", insertable = false, updatable = false),
		 @JoinColumn(name = "ftype", referencedColumnName = "type", insertable = false, updatable = false)
	})
	private Favorite favorite;

	@Column(name = "user_id", length = 50)
	private String userId;
}
