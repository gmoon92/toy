package com.gmoon.springschedulingquartz.server;

import java.util.List;

public interface ServerRepositoryQueryDsl {
  List<Server> getEnabledServers();
  List<Server> getServers(String serverName);
}
