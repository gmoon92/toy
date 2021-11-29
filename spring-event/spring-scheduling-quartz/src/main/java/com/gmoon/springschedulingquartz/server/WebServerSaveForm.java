package com.gmoon.springschedulingquartz.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebServerSaveForm {
  private String name;
  private String publicHost;
  private String privateHost;
  private int port1;
  private int port2;

  public Server createEnabledWebServer() {
    Server webServer = Server.createWebServer(name);
    webServer.setPublicUrl(publicHost, port1);
    webServer.setPrivateHost(privateHost, port2);
    return webServer;
  }
}
