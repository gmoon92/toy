package com.gmoon.hibernatesecondlevelcache.config;

import com.gmoon.hibernatesecondlevelcache.member.MemberRepository;
import com.gmoon.hibernatesecondlevelcache.utils.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.cache.CacheManager;
import java.util.Arrays;

@Slf4j
@Component
public class EhCacheTestLoggingRunner implements ApplicationRunner {

  @Autowired
  CacheManager cacheManager;

  @Autowired
  CacheUtil cacheUtil;

  @Autowired
  MemberRepository memberRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    StopWatch stopWatch = new StopWatch();
    log.info("=======================START=======================");
    log.info("cacheManager : {}", cacheManager);
    Long adminId = 0L;

    call(stopWatch, "hit", adminId);
    call(stopWatch, "caching", adminId);

    cacheUtil.evict(CacheConfig.MEMBER_FIND_BY_ID, adminId);
    call(stopWatch, "evict", adminId);
    call(stopWatch, "caching", adminId);

    cacheUtil.evictAll(CacheConfig.MEMBER_FIND_BY_ID);
    call(stopWatch, "evictAll", adminId);
    call(stopWatch, "caching", adminId);

    long memberId = 1L;
    call(stopWatch, "hit", memberId);
    logging(stopWatch);
    log.info("=======================END=======================");
  }

  private void call(StopWatch stopWatch, String taskName, Long memberId) throws InterruptedException {
    stopWatch.start(taskName);
    memberRepository.getId(memberId);
    stopWatch.stop();
  }

  private void logging(StopWatch stopWatch) {
    Arrays.stream(stopWatch.getTaskInfo())
            .forEach(taskInfo -> log.info("{} : {}", taskInfo.getTaskName(), taskInfo.getTimeMillis()));
  }
}
