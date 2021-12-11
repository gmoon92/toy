package com.gmoon.springquartzcluster.model;

import com.gmoon.springquartzcluster.server.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

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
    return toEntity(webServer);
  }

  public Server toEntity(Server from) {
    from.setPublicUrl(publicHost, port1);
    from.setPrivateHost(privateHost, port2);
    return from;
  }

  public static WebServerSaveForm from(Server server) {
    WebServerSaveForm saveForm = new WebServerSaveForm();
    BeanUtils.copyProperties(server, saveForm);
    return saveForm;
  }
}
