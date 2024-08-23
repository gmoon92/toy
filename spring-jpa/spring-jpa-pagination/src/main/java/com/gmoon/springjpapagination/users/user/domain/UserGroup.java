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
@Table(name = "tb_user_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class UserGroup implements Serializable {

	private static final long serialVersionUID = 6473490199061694565L;

	@Id
	@UuidGenerator
	@Column(length = 50)
	@EqualsAndHashCode.Include
	private String id;

	@Column(length = 50)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	@ToString.Exclude
	private UserGroup parentGroup;
}
