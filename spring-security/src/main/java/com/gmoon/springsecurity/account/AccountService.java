package com.gmoon.springsecurity.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

//  UserDetailsService 인터페이스는
//  인증 관리할때 DAO를 사용해서 어떤 저장소의 데이터를 기준으로 인증 처리를 하기 위함 제약은 없다.
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = Optional.ofNullable(accountRepository.findByUsername(username))
            .orElseThrow(() -> new UsernameNotFoundException(username));

    // User.builder를 사용하여 UserDetails 타입으로 반환
    return User.builder()
            .username(account.getUsername())
            .password(account.getPassword())
            .roles(account.getRole())
            .build();
  }

  public Account createNew(Account account) {
    account.encodePassword(passwordEncoder);
    return this.accountRepository.save(account);
  }
}
