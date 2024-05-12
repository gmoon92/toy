package com.gmoon.springjpapagination.users.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Table(name = "tb_user")
@Entity
public class User {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "id", length = 50)
	private String id;

	@Column(name = "username", length = 20)
	private String username;

	@ManyToOne
	@JoinColumn(name = "user_group_id", referencedColumnName = "id", nullable = false)
	private UserGroup userGroup;
}
