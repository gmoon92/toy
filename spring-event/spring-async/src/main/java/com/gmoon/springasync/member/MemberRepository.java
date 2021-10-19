package com.gmoon.springasync.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface MemberRepository extends CrudRepository<Member, Long> {

  @Query("SELECT m FROM Member m")
  Stream<Member> streamFindAll();
}
