package com.gmoon.springframework.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ScopeAppTestRunner implements ApplicationRunner {

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("proto");
		System.out.println(applicationContext.getBean(Proto.class));
		System.out.println(applicationContext.getBean(Proto.class));
		System.out.println(applicationContext.getBean(Proto.class));

		System.out.println("single");
		System.out.println(applicationContext.getBean(Single.class));
		System.out.println(applicationContext.getBean(Single.class));
		System.out.println(applicationContext.getBean(Single.class));

		System.out.println("proto by single");
		System.out.println(applicationContext.getBean(Single.class).getProto());
		System.out.println(applicationContext.getBean(Single.class).getProto());
		System.out.println(applicationContext.getBean(Single.class).getProto());
	}
}
