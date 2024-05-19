package com.gmoon.springtx.global;

import org.aspectj.lang.Signature;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransactionalUtils {

	public static void logging(Signature signature) {
		TransactionStatus transactionStatus = getTransactionStatus();
		Object transaction = ((DefaultTransactionStatus)transactionStatus)
			 .getTransaction();
		String txName = TransactionSynchronizationManager.getCurrentTransactionName();
		log.info("tx-id({}, {}): {}", transaction.hashCode(), signature, txName);
	}

	public static TransactionStatus getTransactionStatus() {
		return TransactionInterceptor.currentTransactionStatus();
	}
}
