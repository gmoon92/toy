package com.gmoon.springsecuritywhiteship.common;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.gmoon.springsecuritywhiteship.account.Account;
import com.gmoon.springsecuritywhiteship.account.AccountService;
import com.gmoon.springsecuritywhiteship.board.Board;
import com.gmoon.springsecuritywhiteship.board.BoardRepository;

@Component
public class DefaultDataGenerator implements ApplicationRunner {

	@Autowired
	AccountService accountService;

	@Autowired
	BoardRepository boardRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Account admin = Account.newAdmin("admin", "123");
		Account gmoon = Account.newAdmin("gmoon", "123");

		accountService.createNew(admin);
		accountService.createNew(gmoon);

		Board spring = new Board(admin, "spring framework", "ioc container");
		Board hibernate = new Board(gmoon, "hibernate", "envers");

		boardRepository.saveAll(Arrays.asList(spring, hibernate));
	}
}
