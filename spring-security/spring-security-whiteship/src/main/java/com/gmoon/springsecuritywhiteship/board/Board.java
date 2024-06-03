package com.gmoon.springsecuritywhiteship.board;

import com.gmoon.springsecuritywhiteship.account.Account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
