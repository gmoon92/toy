package com.gmoon.demo.apply_form;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.Team;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplyForm {

  @EmbeddedId
  private Id id = new Id();

  @ManyToOne(optional = false)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member;

  @ManyToOne(optional = false)
  @JoinColumn(name = "team_id", insertable = false, updatable = false)
  private Team team;

  @Embeddable
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Id {
    private Member member;
    private Team team;

    public Id(Member member, Team team) {
      this.member = member;
      this.team = team;
    }
  }

}
