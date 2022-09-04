package com.gmoon.junit5.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public void disable(Long memberId) {
		try {
			Member origin = memberRepository.findById(memberId)
				.orElseThrow(MemberNotFoundException::new);
			origin.setEnabled(Boolean.FALSE);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}
}
