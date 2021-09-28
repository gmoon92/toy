package com.gmoon.hibernatesecondlevelcache.config;

import com.gmoon.hibernatesecondlevelcache.member.MemberRepository;
import com.gmoon.hibernatesecondlevelcache.utils.CacheUtil;
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
  CacheUtil cacheUtil;

  @Autowired
  MemberRepository memberRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    StopWatch stopWatch = new StopWatch();
    log.info("=======================START=======================");
    Long memberId = 0L;

    stopWatch.start("hit");
    memberRepository.getId(memberId);
    stopWatch.stop();

    stopWatch.start("caching");
    memberRepository.getId(memberId);
    stopWatch.stop();

    cacheUtil.evict(CacheConfig.MEMBER_FIND_BY_ID, memberId);
    stopWatch.start("evict");
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
