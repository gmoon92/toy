package com.gmoon.springsecurity.common;

import com.gmoon.springsecurity.account.Account;
import com.gmoon.springsecurity.account.AccountService;
import com.gmoon.springsecurity.board.Board;
import com.gmoon.springsecurity.board.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
