package com.gmoon.hibernateenvers.member.domain;

import java.io.Serial;

import org.hibernate.envers.Audited;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Audited
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Member extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 2239812311178022258L;

	@Id
	@GeneratedValue
	@EqualsAndHashCode.Include
	private Long id;

	@Column(length = 50, unique = true, updatable = false)
	private String username;

	@Column(length = 50)
	private String password;

	public static Member createNew(String username, String password) {
		Member member = new Member();
		member.username = username;
		member.password = password;
		return member;
	}
}
