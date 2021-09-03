package com.gmoon.springframework.book;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class BookApplication {

  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
    String[] beanDefinitionNames = context.getBeanDefinitionNames();
    System.out.println(Arrays.toString(beanDefinitionNames));

    BookService bookService1 = context.getBean(BookService.class);
    System.out.println("bookService: " + bookService1);
    BookService bookService2 = (BookService) context.getBean("bookService");
    System.out.println("bookService: " + bookService2);

    boolean isSingleton = bookService1 == bookService2;
    System.out.println("is singleton " + isSingleton);
  }
}
