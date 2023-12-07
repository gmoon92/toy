package com.gmoon.springjpamultidatasource.users.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gmoon.springjpamultidatasource.users.domain.GroupUser;
import com.gmoon.springjpamultidatasource.users.domain.GroupUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupUserService {

	private final GroupUserRepository groupUserRepository;

	public List<GroupUser> getGroupUsers(String groupId) {
		return groupUserRepository.findByGroupId(groupId);
	}

	public void delete(String groupId, String userId) {
		GroupUser groupUser = getGroupUser(groupId, userId);

		groupUser.getGroup().getGroupUsers().size();

		groupUserRepository.delete(groupUser);
	}

	public GroupUser getGroupUser(String groupId, String userId) {
		return groupUserRepository.findByGroupIdAndUserId(groupId, userId);
	}
}
