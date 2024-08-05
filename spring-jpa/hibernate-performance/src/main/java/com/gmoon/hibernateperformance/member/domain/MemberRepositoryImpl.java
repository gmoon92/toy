package com.gmoon.hibernateperformance.member.domain;

import java.util.List;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.hibernateperformance.member.model.Members;
import com.gmoon.hibernateperformance.member.model.QMembers_Data;
import com.gmoon.hibernateperformance.team.domain.QTeamMember;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Member> findAllOfQueryDsl() {
		QMember member = QMember.member;
		QMemberOption memberOption = QMemberOption.memberOption;

		return jpaQueryFactory
			 .select(member)
			 .from(member)
			 .innerJoin(member.memberOption, memberOption).fetchJoin()
			 .fetch();
	}

	@Override
	public Members findAllOfQueryDslWithProjection() {
		QMember member = QMember.member;
		QMemberOption memberOption = QMemberOption.memberOption;

		//        cross join 조심
		//        List<Members.Data> list = jpaQueryFactory
		//                .select(new QMembers_Data(member.id, member.name, memberOption.embeddedMemberOption.enabled))
		//                .from(member)
		//                .fetch();

		//        projection을 사용하여 엔티티 그래프는 도메인을 대상으로한다 vo는 도메인이 아니다.
		//        List<Members.Data> list = jpaQueryFactory
		//                .select(new QMembers_Data(member.id, member.name, memberOption.embeddedMemberOption.enabled))
		//                .from(member)
		//                .innerJoin(member.memberOption, memberOption).fetchJoin()
		//                .fetch();

		List<Members.Data> list = jpaQueryFactory
			 .select(new QMembers_Data(member.id, member.name, memberOption.enabled.enabled))
			 .from(member)
			 .innerJoin(member.memberOption, memberOption)
			 .fetch();

		return new Members(list);
	}

	public long bulkUpdateRetireMembers() {
		QMemberOption memberOption = QMemberOption.memberOption;
		return jpaQueryFactory.update(memberOption)
			 .set(memberOption.retired, false)
			 .where(memberOption.memberId.in(notExistsTeamMember(memberOption)))
			 .execute();
	}

	private Expression<Long> notExistsTeamMember(QMemberOption memberOption) {
		QTeamMember teamMember = QTeamMember.teamMember;
		return JPAExpressions
			 .select(teamMember.id.memberId)
			 .from(teamMember)
			 .where(teamMember.id.memberId.eq(memberOption.memberId));
	}
}
