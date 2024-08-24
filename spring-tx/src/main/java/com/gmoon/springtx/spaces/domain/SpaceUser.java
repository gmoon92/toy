package com.gmoon.springtx.spaces.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "tb_space_user")
@Getter
public class SpaceUser implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	@Column(length = 50, nullable = false, updatable = false)
	private String userId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "space_id")
	private Space space;
}
