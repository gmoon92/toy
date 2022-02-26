package com.gmoon.springsecurityjwt.team;

import static com.gmoon.springsecurityjwt.team.QTeam.*;
import static com.gmoon.springsecurityjwt.team.QTeamUser.*;
import static com.gmoon.springsecurityjwt.user.QUser.*;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeamRepositoryCustomImpl implements TeamRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Team> findAll() {
		return jpaQueryFactory.select(team)
			.from(team)
			.leftJoin(team.users, teamUser).fetchJoin()
			.leftJoin(teamUser.user, user).fetchJoin()
			.fetch();
	}

	@Override
	public Team getById(Long teamId) {
		return jpaQueryFactory.select(QTeam.create(team.id, team.name))
			.from(team)
			.fetchOne();
	}
}
