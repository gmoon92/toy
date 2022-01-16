package com.gmoon.springsecurityjwt.team;

import java.util.List;

import com.gmoon.springsecurityjwt.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeamRepositoryCustomImpl implements TeamRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Team> findAll() {
		QTeam team = QTeam.team;
		QUser user = QUser.user;
		return jpaQueryFactory.select(team)
			.from(team)
			.leftJoin(team.users, user).fetchJoin()
			.fetch();
	}

	@Override
	public Team getById(Long teamId) {
		QTeam team = QTeam.team;
		return jpaQueryFactory.select(QTeam.create(team.id, team.name))
			.from(team)
			.fetchOne();
	}
}
