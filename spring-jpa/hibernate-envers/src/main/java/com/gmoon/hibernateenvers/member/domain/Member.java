package com.gmoon.hibernateenvers.member.domain;

import java.io.Serial;

import org.hibernate.envers.Audited;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Audited
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
@ToString
public class Member extends BaseTrackingEntity {

	@Serial
	private static final long serialVersionUID = 2239812311178022258L;

	@Id
	@GeneratedValue
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
