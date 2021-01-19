package com.gmoon.demo.domain;

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
public class AccountOptionEmb {

  private boolean enabled;

  private LocalDateTime enabledDt;

  public static AccountOptionEmb enabled() {
    AccountOptionEmb accountOption = new AccountOptionEmb();
    accountOption.enabled = true;
    accountOption.enabledDt = LocalDateTime.now();
    return accountOption;
  }

  public static AccountOptionEmb disabled() {
    AccountOptionEmb accountOption = new AccountOptionEmb();
    accountOption.enabled = false;
    accountOption.enabledDt = null;
    return accountOption;
  }
}
