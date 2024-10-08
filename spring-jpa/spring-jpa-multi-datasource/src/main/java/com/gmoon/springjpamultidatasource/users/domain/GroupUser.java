package com.gmoon.springjpamultidatasource.users.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "tb_group_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupUser implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	@ToString.Include
	private String id;

	@Column(length = 50, nullable = false, updatable = false)
	private String userId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "group_id")
	@JsonIgnore
	private Group group;
}
