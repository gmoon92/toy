package com.gmoon.springasync.member;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Member implements Serializable {
  private static final long serialVersionUID = 455845596925771520L;
  private static final String INIT_PASSWORD = "111111";

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  private Member(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public static Member createNew(String email) {
    return new Member(email, INIT_PASSWORD);
  }
}
