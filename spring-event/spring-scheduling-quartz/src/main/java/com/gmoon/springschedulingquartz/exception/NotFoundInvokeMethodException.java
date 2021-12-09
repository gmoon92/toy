package com.gmoon.springschedulingquartz.exception;

public class NotFoundInvokeMethodException extends RuntimeException {
  public NotFoundInvokeMethodException() {
    super("Not found invocable method.");
  }
}
