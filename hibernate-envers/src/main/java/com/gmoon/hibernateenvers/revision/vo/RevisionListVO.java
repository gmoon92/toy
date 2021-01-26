package com.gmoon.hibernateenvers.revision.vo;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.global.vo.BaseSearchVO;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Date;

@ToString
public class RevisionListVO {

  @Getter
  @Setter
  private SearchVO searchVO = new SearchVO();

  @Getter
  @Setter
  public static class SearchVO extends BaseSearchVO {

    public enum SearchType {
      EMPTY, MEMBER_NAME, TARGET_TEAM_NAME, TARGET_MEMBER_NAME
    }

    private RevisionTarget revisionTarget;

    private SearchType searchType = SearchType.EMPTY;

    @TODO("order by type safe code...?")
    public SearchVO() {
      super(Sort.by(Sort.Direction.DESC, "revision.createdDt"));
    }

    public void setPage(Integer page) {
      this.page = page;
    }
  }

  @Getter
  @ToString
  public static class DataVO implements Serializable {
    private final long serialVersionUID = 4214996561651068387L;

    private Long rev;

    private Date revDate;

    private String memberId;

    private String memberName;

    private RevisionTarget revisionTarget;

    private Object entityId;

    private String targetTeamName;

    private String targetMemberName;

    @QueryProjection
    public DataVO(Long rev, Date revDate, String memberId, String memberName, RevisionTarget revisionTarget, byte[] entityId, String targetTeamName, String targetMemberName) {
      this.rev = rev;
      this.revDate = revDate;
      this.memberId = memberId;
      this.memberName = memberName;
      this.revisionTarget = revisionTarget;
      this.entityId = RevisionConverter.deSerializedObject(entityId);
      this.targetTeamName = targetTeamName;
      this.targetMemberName = targetMemberName;
    }
  }


}
