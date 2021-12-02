package com.gmoon.springschedulingquartz.job_store.constants;

import com.gmoon.springschedulingquartz.exception.NotSupportedInitializerException;

public final class QuartzColumnValue {
  public static final String ENABLED = "'1'";

  private QuartzColumnValue() {
    throw new NotSupportedInitializerException();
  }
}
