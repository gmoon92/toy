package com.gmoon.quickperf.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "member")
public class MemberOption {

	@Id
	private Long id;

	@MapsId
	@OneToOne
	private Member member;

	private boolean enabled;

	static MemberOption defaultOption(Member member) {
		MemberOption option = new MemberOption();
		option.member = member;
		option.enabled = true;
		return option;
	}
}
