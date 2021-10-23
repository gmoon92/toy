package com.gmoon.springasync.mail;

public class SendMailException extends IllegalArgumentException {
  private static final long serialVersionUID = 344115217665250394L;

  private static final String EXCEPTION_MESSAGE = "메일 발송 실패";

  public SendMailException() {
    super(EXCEPTION_MESSAGE);
  }

  public SendMailException(Throwable throwable) {
    super(EXCEPTION_MESSAGE, throwable);
  }
}
