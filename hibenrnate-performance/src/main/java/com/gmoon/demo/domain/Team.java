package com.gmoon.demo.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Team(String name, List<Member> members) {
        this.name = name;
        this.members = members;
    }

    public static Team newInstance(String name) {
        return Team.builder()
                .name(name)
                .build();
    }
}
