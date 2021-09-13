package com.gmoon.springframework.inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PooService {

  private PooRepository pooRepository;

  public PooService(PooRepository pooRepository) {
    this.pooRepository = pooRepository;
  }

//  @Autowired
//  public void setPooRepository(PooRepository pooRepository) {
//    this.pooRepository = pooRepository;
//  }
}
