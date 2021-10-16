package com.gmoon.springasync.config;

import com.gmoon.springasync.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class SampleDataRunner implements ApplicationRunner {
  private final MemberRepository memberRepository;

  @Override
  public void run(ApplicationArguments args) {
    memberRepository.saveAll(Arrays.asList(
            // todo: send mail member add.
//            Member.createNew("test1@gmail.com"),
//            Member.createNew("test2@gmail.com")
    ));
  }
}
