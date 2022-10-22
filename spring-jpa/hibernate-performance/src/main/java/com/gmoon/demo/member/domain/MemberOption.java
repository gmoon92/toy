package com.gmoon.demo.member.domain;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import com.gmoon.demo.member.model.MemberOptionUpdate;

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

	void changeOptions(MemberOptionUpdate memberOptionUpdate) {
		retired = memberOptionUpdate.isRetired();
	}
}
