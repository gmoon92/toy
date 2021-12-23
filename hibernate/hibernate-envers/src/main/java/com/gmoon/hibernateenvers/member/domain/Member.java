package com.gmoon.hibernateenvers.member.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Audited
@Entity
@Table(name = "member")
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTrackingEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "username", length = 50, unique = true, updatable = false)
	private String username;

	@Column(name = "password", length = 50)
	private String password;

	public static Member createNew(String username, String password) {
		Member member = new Member();
		member.username = username;
		member.password = password;
		return member;
	}
}
