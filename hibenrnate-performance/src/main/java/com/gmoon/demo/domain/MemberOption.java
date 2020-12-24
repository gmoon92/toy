package com.gmoon.demo.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOption {

    @Id
    private Long memberId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Member member;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "retired")
    private boolean retired;

    public static MemberOption defaultOption(Member member) {
        MemberOption option = new MemberOption();
        option.setMember(member);
        option.setDefaultOption();
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


}
