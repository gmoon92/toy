package com.gmoon.demo.apply_form;

import com.gmoon.demo.base.EntityId;
import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.Team;
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
import javax.persistence.Table;

@Table
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@ToString
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

  private String title;

  private String content;

  ApplyForm(Member member, Team team) {
    this.member = member;
    this.team = team;
    this.id = new Id(member, team);
  }

  public static ApplyForm newInstance(Member member, Team team) {
    return new ApplyForm(member, team);
  }

  public void fillOut(String title, String content) {
    this.title = title;
    this.content = content;
  }

  @Embeddable
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  static class Id extends EntityId {

    private static final long serialVersionUID = -4188030215536759764L;

    @Column(name = "member_id")
    private Long member;

    @Column(name = "team_id")
    private Long team;

    Id(Member member, Team team) {
      this.member = member.getId();
      this.team = team.getId();
    }
  }

}
