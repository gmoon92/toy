package com.gmoon.javacore.concurrency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedResourcesTest {

  @Disabled("여러 스레드가 공유하는 count 인스턴스의 값이 항상 동일하진 않는다.")
  @RepeatedTest(100)
  void 동시성_공유자원() {
    Count count = new Count();

    for (int i = 0; i < 100; i++) {
      new Thread(count::plus)
              .start();
    }

    assertThat(count.value).isEqualTo(100);
  }


  static class Count {
    private int value;

    public void plus() {
      ++value;
    }
  }
}
