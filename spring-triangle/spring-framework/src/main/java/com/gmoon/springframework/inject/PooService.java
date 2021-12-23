package com.gmoon.springframework.inject;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

@Service
public class PooService {

	@Autowired
	private AutowireCapableBeanFactory factory;

	private PooRepository pooRepository;

	@PostConstruct
	public void after() {
		System.out.println("Initialization after... PooRepository" + factory.getBean(PooRepository.class));
	}

	public PooService(PooRepository pooRepository,
		@Qualifier(value = "moonRepository") PooRepository pooRepository2,
		PooRepository moonRepository,
		List<PooRepository> pooRepositorys) {
		System.out.println("PooRepository primary type is " + pooRepository);
		System.out.println("PooRepository @Qualifier type is " + pooRepository2);
		System.out.println("PooRepository bean id type is " + moonRepository);
		pooRepositorys.forEach(System.out::println);
		this.pooRepository = pooRepositorys.stream().findFirst()
			.orElse(null);
	}

	//  @Autowired
	//  public void setPooRepository(PooRepository pooRepository) {
	//    this.pooRepository = pooRepository;
	//  }

}
