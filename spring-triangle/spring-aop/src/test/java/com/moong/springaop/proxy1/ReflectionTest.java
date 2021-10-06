package com.moong.springaop.proxy1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionTest {
  private Reflection rf;

  @BeforeAll
  void before() {
    rf = new Reflection();
  }

  @Test
  void invoke() throws Exception {
    String name = "Moon";

    assertThat(name.length()).isEqualTo(4);
    assertThat(name.length()).isEqualTo(rf.stringLength(name));
    assertThat(name.charAt(0)).isEqualTo('M');
    assertThat(name.charAt(0)).isEqualTo(rf.stringCharAt(name, 0));
  }

  @Test
  @DisplayName("다이나믹 프로젝트는 이런식으로 Proxy Object가 생성되지 않는다.")
  void dateObject() throws Exception {
    String str = (String) rf.createObject("java.lang.String");
    str = "test";

    assertThat(str.length()).isEqualTo(4);
  }
}
