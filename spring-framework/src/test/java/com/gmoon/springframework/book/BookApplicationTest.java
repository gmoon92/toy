package com.gmoon.springframework.book;

import com.gmoon.springframework.config.ApplicationConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class BookApplicationTest {

  public static void main(String[] args) {
    ApplicationContext context = getAnnotationApplicationContext();
    String[] beanDefinitionNames = context.getBeanDefinitionNames();
    System.out.println(Arrays.toString(beanDefinitionNames));

    BookService bookService1 = context.getBean(BookService.class);
    System.out.println("bookService1: " + bookService1);
    BookService bookService2 = (BookService) context.getBean("bookService");
    System.out.println("bookService2: " + bookService2);

    boolean isSingleton = bookService1 == bookService2;
    System.out.println("is singleton " + isSingleton);
  }

  private static ApplicationContext getXmlApplicationContext() {
    return new ClassPathXmlApplicationContext("application-bean-test.xml");
  }

  private static ApplicationContext getAnnotationApplicationContext() {
    return new AnnotationConfigApplicationContext(ApplicationConfig.class);
  }

}
