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

@Getter
@Entity
@ToString(exclude = "member")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
