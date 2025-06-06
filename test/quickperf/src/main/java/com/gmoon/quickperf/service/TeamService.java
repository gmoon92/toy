package com.gmoon.quickperf.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.quickperf.domain.Member;
import com.gmoon.quickperf.domain.Team;
import com.gmoon.quickperf.repository.TeamRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TeamService {
	private final TeamRepository repository;

	public Team findByName(String name) {
		return repository.findByName(name);
	}

	@Transactional
	public void moveAllMemberToAssignedNewTeam(List<Member> members, Long newTeamId) {
		Team newTeam = repository.findById(newTeamId)
			 .orElseThrow(EntityNotFoundException::new);

		removeOriginTeam(members);
		newAssignedTeam(members, newTeam);
	}

	private void removeOriginTeam(List<Member> members) {
		for (Member member : members) {
			member.removeTeam();
		}
	}

	private void newAssignedTeam(List<Member> members, Team newTeam) {
		for (Member member : members) {
			newTeam.addMember(member);
		}
	}
}
