package com.gmoon.springtx.favorites.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_favorite_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
public class FavoriteUser implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
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
