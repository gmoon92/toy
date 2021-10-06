package com.gmoon.hibernatesecondlevelcache.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Member implements Serializable {

  private Long id;
  private String name;

}
