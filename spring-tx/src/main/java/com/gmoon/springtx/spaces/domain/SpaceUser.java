package com.gmoon.springtx.spaces.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "tb_space_user")
@Getter
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
public class SpaceUser implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(length = 50)
	private String id;

	@Column(name = "user_id", length = 50, nullable = false, updatable = false)
	private String userId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "space_id")
	private Space space;
}
