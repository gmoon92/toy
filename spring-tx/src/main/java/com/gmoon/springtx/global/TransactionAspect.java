package com.gmoon.springtx.global;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class TransactionAspect {

	@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional) "
		+ "&& execution(* com.gmoon..*(..))")
	public void pcdTransactional() {
	}

	@Around("pcdTransactional()")
	public Object aroundAdvice(ProceedingJoinPoint jp) {
		try {
			MethodSignature signature = (MethodSignature)jp.getSignature();
			TransactionalUtils.logging(signature);

			Object result = jp.proceed();

			TransactionalUtils.logging(signature);
			return result;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
