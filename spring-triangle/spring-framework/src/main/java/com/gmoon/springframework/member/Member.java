package com.gmoon.springframework.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Member {

  private String name;
  private LocalDateTime createdDt;
  private MemberStatus status;
}
