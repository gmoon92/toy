package com.gmoon.hibernatesecondlevelcache.config;

import com.gmoon.hibernatesecondlevelcache.member.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

@Slf4j
@Component
public class EhCacheTestLoggingRunner implements ApplicationRunner {

  @Autowired
  MemberRepository memberRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    StopWatch stopWatch = new StopWatch();
    log.info("=======================START=======================");
    Long memberId = 0L;

    stopWatch.start("cache hit");
    memberRepository.getId(memberId);
    stopWatch.stop();

    stopWatch.start("caching data");
    memberRepository.getId(memberId);
    stopWatch.stop();

    logging(stopWatch);
    log.info("=======================END=======================");
  }

  private void logging(StopWatch stopWatch) {
    Arrays.stream(stopWatch.getTaskInfo())
            .forEach(taskInfo -> log.info("{} : {}", taskInfo.getTaskName(), taskInfo.getTimeSeconds()));
  }
}
