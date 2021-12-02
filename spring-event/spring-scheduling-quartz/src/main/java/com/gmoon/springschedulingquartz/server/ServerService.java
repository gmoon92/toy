package com.gmoon.springschedulingquartz.server;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerService {
  private final ServerRepository serverRepository;

  @Transactional(readOnly = true)
  @Cacheable(value = ServerCacheNames.SERVER_ALL)
  public List<Server> getEnabledServers() {
    return serverRepository.getEnabledServers();
  }

  @Transactional(readOnly = true)
  @Cacheable(value = ServerCacheNames.SERVER_FIND_BY_NAME, key = "#serverName")
  public Server getServer(String serverName) {
    return serverRepository.findServerByName(serverName);
  }

  @Transactional
  public WebServerSaveForm createWebServer(WebServerSaveForm saveForm) {
    Server savedServer = serverRepository.save(saveForm.createEnabledWebServer());
    return WebServerSaveForm.of(savedServer);
  }
}
