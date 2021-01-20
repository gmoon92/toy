package com.gmoon.springsecurity.account;

import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class Member {

  @Id @GeneratedValue
  private Long id;

  @Column(unique = true)
  private String username;

  private String password;

  private String role;

//  java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
  public void encodePassword(PasswordEncoder passwordEncoder) {
    this.password = passwordEncoder.encode(this.password);
  }
}
