package com.gmoon.springjpapagination.users.user.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "tb_user")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class User implements Serializable {

	private static final long serialVersionUID = -7636049955759609730L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	@EqualsAndHashCode.Include
	private String id;

	@Column(name = "username", length = 20)
	private String username;

	@ManyToOne
	@JoinColumn(name = "user_group_id", referencedColumnName = "id", nullable = false)
	@ToString.Exclude
	private UserGroup userGroup;
}
