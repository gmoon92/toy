package com.gmoon.springframework.scope;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.ws.Action;

@Component
public class Single {

  @Autowired
  ObjectProvider<Proto> proto;

  public Proto getProto() {
    return proto.getIfAvailable();
  }
}
