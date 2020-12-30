package com.gmoon.demo.repository;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.domain.Member;
import com.gmoon.demo.domain.MemberOption;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@RequiredArgsConstructor
class MemberOptionRepositoryTest extends BaseRepositoryTest {

    final MemberRepository memberRepository;

    final MemberOptionRepository memberOptionRepository;

    Member user;

    @BeforeAll
    static void setup(@Autowired MemberRepository memberRepository) {
        memberRepository.deleteAllInBatch();
        memberRepository.save(Member.newInstance("gmoon"));
    }


    @BeforeEach
    void init() {
        user = memberRepository.findByName("gmoon");
    }

    /**
     * org.springframework.orm.jpa.JpaSystemException: ids for this class must be manually assigned before calling save()
     * -> 영속 상태가 아니기 때문에 ID가 존재하지 않았음, Id가 아니기 때문
     * @see MemberOption#setMember(Member)
     */
    @Test
    @DisplayName("대상 테이블 OneToOne 저장")
    void tetSave() {
        MemberOption option = MemberOption.newInstance(user);
        option.enabled();
        memberOptionRepository.save(option);
    }
}