package com.gmoon.hibernatesequencegenerator.domain;

import com.gmoon.hibernatesequencegenerator.constants.ColumnLength;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class Company {

  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid2")
  @Column(length = ColumnLength.SYSTEM_UUID)
  private String id;

  private String name;

  public Company(String name) {
    this.name = name;
  }
}
