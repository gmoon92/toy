package com.gmoon.hibernateperformance.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.member.domain.MemberRepository;
import com.gmoon.hibernateperformance.member.model.MemberOptionUpdateRequestVO;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public void updateMemberOption(MemberOptionUpdateRequestVO requestVO) {
		Long memberId = requestVO.getMemberId();
		boolean retired = requestVO.isRetired();

		Member member = memberRepository.getById(memberId);
		member.retire(retired);
	}
}
