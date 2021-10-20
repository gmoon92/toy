package com.gmoon.demo.team;

import com.gmoon.demo.member.domain.Member;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import java.io.Serializable;

@Entity
@ToString
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

  @EmbeddedId
  private Id id = new Id();

  @MapsId("memberId")
  @ManyToOne(optional = false)
  @JoinColumn(name = "member_id", referencedColumnName = "id", insertable = false, updatable = false)
  private Member member;

  @MapsId("teamId")
  @ManyToOne(optional = false)
  @JoinColumn(name = "team_id", referencedColumnName = "id", insertable = false, updatable = false)
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

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    Id(Member member, Team team) {
      this.memberId = member.getId();
      this.teamId = team.getId();
    }
  }

}
