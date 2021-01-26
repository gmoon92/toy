package com.gmoon.hibernateenvers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
public class HibernateEnversApplication {

  public static void main(String[] args) {
    SpringApplication.run(HibernateEnversApplication.class, args);
  }

}
