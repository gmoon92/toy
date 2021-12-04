package com.gmoon.hibernatesequencegenerator.domain;

import com.gmoon.hibernatesequencegenerator.constants.ColumnLength;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompanyRepositoryTest {
  @Autowired
  EntityManager entityManager;

  @Autowired
  CompanyRepository companyRepository;

  @Test
  @DisplayName("하이버네이트 UUID2 ID 생성 전략 36 길이를 생성한다.")
  void testUUIDGeneratorLength() {
    // given
    Company company = new Company("google");

    // when
    company = companyRepository.save(company);

    // then
    String companyId = company.getId();
    byte[] uuid = companyId.getBytes(StandardCharsets.UTF_8);
    assertThat(uuid).hasSize(ColumnLength.SYSTEM_UUID);
  }

  @AfterEach
  void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}