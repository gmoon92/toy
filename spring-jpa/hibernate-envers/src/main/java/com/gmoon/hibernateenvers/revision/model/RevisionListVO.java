package com.gmoon.hibernateenvers.revision.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.domain.Sort;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.global.model.BaseSearchVO;
import com.gmoon.hibernateenvers.global.utils.PageUtils;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.revision.domain.QRevision;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class RevisionListVO {

	private SearchVO search = new SearchVO();

	@Getter
	@Setter
	public static class SearchVO extends BaseSearchVO {

		private KeywordCondition keywordCondition = KeywordCondition.MEMBER_NAME;

		private RevisionTarget revisionTarget;

		@TODO("order by type safe code...?")
		public SearchVO() {
			super(PageUtils.sort(Sort.Direction.DESC, QRevision.revision.createdAt));
		}

		public enum KeywordCondition {
			UPDATED_BY, MEMBER_NAME
		}
	}

	@Getter
	@ToString
	public static class DataVO implements Serializable {

		@Serial
		private static final long serialVersionUID = 4214996561651068387L;

		private final Long revisionId;
		private final Long revisionTime;
		private final Object entityId;
		private final RevisionTarget revisionTarget;
		private final String updatedBy;
		private final String updatedByUsername;
		private final String targetMemberName;

		@QueryProjection
		public DataVO(
			 Long revisionId,
			 Instant revisionAt,
			 byte[] entityId,
			 RevisionTarget revisionTarget,
			 String updatedBy,
			 String updatedByUsername,
			 String targetMemberName
		) {
			this.revisionId = revisionId;
			this.revisionTime = revisionAt.toEpochMilli();
			this.updatedBy = updatedBy;
			this.updatedByUsername = updatedByUsername;
			this.targetMemberName = targetMemberName;
			this.revisionTarget = revisionTarget;
			this.entityId = RevisionConverter.deSerializedObject(entityId);
		}
	}
}
