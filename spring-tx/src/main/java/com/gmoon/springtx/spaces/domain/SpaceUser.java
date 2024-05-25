package com.gmoon.springtx.spaces.domain;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
