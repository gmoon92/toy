package com.gmoon.demo.member.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Getter
@Embeddable
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmbeddedMemberOption {

  private boolean enabled;

  private LocalDateTime enabledDt;

  public static EmbeddedMemberOption enabled() {
    EmbeddedMemberOption accountOption = new EmbeddedMemberOption();
    accountOption.enabled = true;
    accountOption.enabledDt = LocalDateTime.now();
    return accountOption;
  }

  public static EmbeddedMemberOption disabled() {
    EmbeddedMemberOption accountOption = new EmbeddedMemberOption();
    accountOption.enabled = false;
    accountOption.enabledDt = null;
    return accountOption;
  }
}
