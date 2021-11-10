package com.gmoon.springsecuritywhiteship.board;

import com.gmoon.springsecuritywhiteship.account.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private Account author;

  private String title;
  private String content;

  public Board(Account author, String title, String content) {
    this.author = author;
    this.title = title;
    this.content = content;
  }
}
