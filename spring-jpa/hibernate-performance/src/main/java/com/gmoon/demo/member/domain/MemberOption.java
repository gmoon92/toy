package com.gmoon.demo.member.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(exclude = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = {"memberId", "member"})
public class MemberOption {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	@MapsId
	@OneToOne
	@PrimaryKeyJoinColumn(name = "member_id", referencedColumnName = "id")
	private Member member;

	@Embedded
	@AttributeOverrides({
		 @AttributeOverride(name = "enabled", column = @Column(name = "member_enabled")),
		 @AttributeOverride(name = "enabledDt", column = @Column(name = "member_enabled_dt"))
	})
	private Enabled enabled;

	@Column(name = "retired")
	private boolean retired;

	static MemberOption defaultOption(Member member) {
		MemberOption option = new MemberOption();
		option.member = member;
		option.disabled();
		return option;
	}

	void enabled() {
		this.enabled = Enabled.enabled();
		this.retired = true;
	}

	void disabled() {
		this.enabled = Enabled.disabled();
		this.retired = false;
	}

	void retire(boolean retired) {
		this.retired = retired;
		if (!retired) {
			this.enabled = Enabled.disabled();
		}
	}
}
