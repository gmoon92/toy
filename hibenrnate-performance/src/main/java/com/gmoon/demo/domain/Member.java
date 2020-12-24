package com.gmoon.demo.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
@NamedEntityGraph(name = "Member.options"
        , attributeNodes = { @NamedAttributeNode("memberOption") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Fetch(FetchMode.JOIN)
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberOption memberOption;

    @Builder
    private Member(String name, Team team, MemberOption memberOption) {
        this.name = name;
        this.team = team;
        this.memberOption = memberOption;
    }

    public static Member newInstance(String name) {
        Member member = new Member();
        member.setName(name);
        return member;
    }
}
