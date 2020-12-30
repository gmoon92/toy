package com.gmoon.demo.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOption {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne
//    @JoinColumn(name = "member_id", referencedColumnName = "memberId")
    @PrimaryKeyJoinColumn(name = "member_id", referencedColumnName = "memberId")
    private Member member;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "retired")
    private boolean retired;

    public static MemberOption defaultOption() {
        MemberOption option = new MemberOption();
        option.setDefaultOption();
        return option;
    }

    public static MemberOption newInstance(Member member) {
        MemberOption option = defaultOption();
        option.setMember(member);
        return option;
    }

    protected void setMember(Member member) {
        this.member = member;
        this.memberId = member.getId();
    }

    private void setDefaultOption() {
        this.enabled = false;
        this.retired = false;
    }

    public void enabled() {
        this.enabled = true;
        this.retired = false;
    }
}
