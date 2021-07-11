package com.gmoon.demo.domain;

import com.gmoon.demo.service.MemberOptionUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private AccountOptionEmb accountOptionEmb;

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
        disabled();
    }

    public void enabled() {
        this.accountOptionEmb = AccountOptionEmb.enabled();
        this.retired = false;
    }

    public void disabled() {
        this.accountOptionEmb = AccountOptionEmb.disabled();
        this.retired = false;
    }

    public void changeOptions(MemberOptionUpdate memberOptionUpdate) {
        retired = memberOptionUpdate.isRetired();
    }
}
