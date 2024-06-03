package com.gmoon.resourceserver.bookmark;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_bookmark", uniqueConstraints = {@UniqueConstraint(name = "u_name", columnNames = {"name"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "name")
@ToString(of = "name")
public class Bookmark implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	public static Bookmark create(String name) {
		Bookmark bookMark = new Bookmark();
		bookMark.name = name;
		return bookMark;
	}
}
