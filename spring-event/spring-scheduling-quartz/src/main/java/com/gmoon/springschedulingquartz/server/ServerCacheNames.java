package com.gmoon.springschedulingquartz.server;

import com.gmoon.springschedulingquartz.exception.NotSupportedInitializerException;

public final class ServerCacheNames {
  public static final String SERVER_ALL = "SERVER_ALL";
  public static final String SERVER_FIND_BY_NAME = "SERVER_FIND_BY_NAME";

  private ServerCacheNames() {
    throw new NotSupportedInitializerException();
  }
}
