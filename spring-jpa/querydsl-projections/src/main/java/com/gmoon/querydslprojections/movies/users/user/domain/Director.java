package com.gmoon.querydslprojections.movies.users.user.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_director")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
public class Director extends User {

	public Director(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
