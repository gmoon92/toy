package com.gmoon.springjpapagination.users.user.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class User implements Serializable {

	private static final long serialVersionUID = -7636049955759609730L;

	@Id
	@UuidGenerator
	@Column(length = 50)
	@EqualsAndHashCode.Include
	private String id;

	@Column(length = 20)
	private String username;

	@ManyToOne
	@JoinColumn(name = "user_group_id", referencedColumnName = "id", nullable = false)
	@ToString.Exclude
	private UserGroup userGroup;
}
