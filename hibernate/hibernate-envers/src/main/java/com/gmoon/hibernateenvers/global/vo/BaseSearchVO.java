package com.gmoon.hibernateenvers.global.vo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSearchVO extends BasePageable {

  public BaseSearchVO(Sort sort) {
    super(sort);
  }

  private Date startDt;

  private Date endDt;

  private String searchKeyword;

}