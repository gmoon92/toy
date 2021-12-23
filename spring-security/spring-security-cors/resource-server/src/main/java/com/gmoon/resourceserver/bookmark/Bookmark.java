package com.gmoon.resourceserver.bookmark;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(of = "name")
@EqualsAndHashCode(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {@UniqueConstraint(name = "u_name", columnNames = {"name"})})
public class Bookmark implements Serializable {
	@Id
	@GeneratedValue
	private Long id;

	private String name;

	public static Bookmark create(String name) {
		Bookmark bookMark = new Bookmark();
		bookMark.name = name;
		return bookMark;
	}
}
