package com.gmoon.demo.repository;

import com.gmoon.demo.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@DataJpaTest
class MemberRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void init() {
//        flush 상관 없이 즉시 쿼리 실행
        memberRepository.deleteAllInBatch();
        memberRepository.save(Member.builder()
                .name("gmoon")
                .build());
        flushAndClear();
        log.info("=============================================================================");
        log.info("=============================================================================");
        log.info("=============================================================================");
        log.info("=============================================================================");
    }

    @Test
    void testGetOne() {
//        OneToOne member 조회되면 option도 1번 조회
        Member member = memberRepository.findById(1L)
                .orElse(null);
        log.info("member : {}", member);
    }

    @AfterEach
    void tearDown() {
        log.info("=============================================================================");
        log.info("=============================================================================");
        log.info("=============================================================================");
        log.info("=============================================================================");
        flushAndClear();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}