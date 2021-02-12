package com.gmoon.springsecurity.account;

public class AccountContext {

  private static final ThreadLocal<Member> ACCOUNT_THREAD_LOCAL
          = new ThreadLocal<>();

  public static void setMember(Member member) {
    ACCOUNT_THREAD_LOCAL.set(member);
  }

  public static Member getMember() {
    return ACCOUNT_THREAD_LOCAL.get();
  }
}
