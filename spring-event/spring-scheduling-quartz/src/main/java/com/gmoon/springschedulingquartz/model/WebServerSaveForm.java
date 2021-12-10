package com.gmoon.springschedulingquartz.model;

import com.gmoon.springschedulingquartz.server.Server;
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

  public static WebServerSaveForm of(Server server) {
    WebServerSaveForm saveForm = new WebServerSaveForm();
    saveForm.name = server.getName();
    saveForm.publicHost = server.getPublicHost();
    saveForm.privateHost = server.getPrivateHost();
    saveForm.port1 = server.getPort1();
    saveForm.port2 = server.getPort2();
    return saveForm;
  }
}
