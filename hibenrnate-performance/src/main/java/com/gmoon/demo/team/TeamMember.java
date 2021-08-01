package com.gmoon.demo.team;

import com.gmoon.demo.member.domain.Member;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

  @EmbeddedId
  private Id id;

  @ManyToOne(optional = false)
  private Member member;

  @ManyToOne(optional = false)
  private Team team;

  public TeamMember(Member member, Team team) {
    this.member = member;
    this.team = team;
    this.id = new Id(member, team);
  }

  @Embeddable
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Id implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "team_id")
    private Long teamId;

    Id(Member member, Team team) {
      this.memberId = member.getId();
      this.teamId = team.getId();
    }
  }

}
