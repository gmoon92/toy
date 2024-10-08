package com.gmoon.springtx.spaces.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "tb_space")
@Getter
public class Space implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	@Column(nullable = false, length = 50)
	private String name;

	@OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SpaceUser> spaceUsers = new ArrayList<>();
}
