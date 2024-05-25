package com.gmoon.springtx.spaces.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "tb_space")
@Getter
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
public class Space implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(length = 50)
	private String id;

	@Column(nullable = false, length = 50)
	private String name;

	@OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SpaceUser> spaceUsers = new ArrayList<>();
}
