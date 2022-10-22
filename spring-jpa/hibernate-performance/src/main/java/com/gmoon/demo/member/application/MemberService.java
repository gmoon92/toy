package com.gmoon.demo.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.domain.MemberRepository;
import com.gmoon.demo.member.model.MemberOptionUpdateRequestVO;

import lombok.RequiredArgsConstructor;

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
