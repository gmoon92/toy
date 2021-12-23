package com.gmoon.springasync.mail;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class MailServiceTest {
	static final int MILLISECOND_OF_WAIT = 3_000;

	@Autowired
	MailService mailService;

	@Autowired
	BeanFactory beanFactory;

	@Mock
	MailSender mailSender;

	@MockBean
	AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler;

	@Test
	@DisplayName("현재 프로젝트에서 @EnableAsync에서 주입하고 있는 Task Executor 를 알아보자")
	void taskExecutorName() {
		// given
		Class<TaskExecutor> asyncExecutor = TaskExecutor.class;

		// when
		Object executor = beanFactory.getBean(asyncExecutor);

		// then
		assertThat(executor)
			.isNotInstanceOf(SimpleAsyncTaskExecutor.class)
			.isInstanceOf(ThreadPoolTaskExecutor.class);
	}

	@Test
	@DisplayName("비동기시 초대 메일을 보낸 서버 주소를 반환 받는다.")
	void sendInviteMailFromServerWillReturn() throws InterruptedException {
		// given
		String publicUrl = "https://gmoon92.github.io";

		// when
		Future<String> future = mailService.sendInviteMailFromServerWillReturn(publicUrl);

		// then
		while (future.isDone()) {
			Assumptions.assumingThat(future.isDone(),
				() -> assertThat(future.get()).isEqualTo(publicUrl));
			log.info("Alive thread...");
			Thread.sleep(MILLISECOND_OF_WAIT);
		}
	}

	@Test
	@DisplayName("void 비동기 예외 처리")
	void sendMailIfPublicUrlIsBlankThrow() throws InterruptedException {
		// given
		String publicUrl = "";

		// when
		mailService.sendMailIfPublicUrlIsBlankThrow(publicUrl);
		Thread.sleep(MILLISECOND_OF_WAIT);

		// then
		then(asyncUncaughtExceptionHandler)
			.should(times(1))
			.handleUncaughtException(any(), any(), any());
	}

	@Test
	@DisplayName("반환 타입이 있는 비동기 예외는 핸들러를 통해 잡을 수 없다.")
	void sendMailWillReturnIfPublicUrlIsBlankThrow() {
		// given
		String blankUrl = "";

		// when
		Future<String> future = mailService.sendMailWillReturnIfPublicUrlIsBlankThrow(blankUrl);

		// then
		assertThatThrownBy(future::get)
			.isInstanceOf(ExecutionException.class);
		then(asyncUncaughtExceptionHandler)
			.should(never())
			.handleUncaughtException(any(), any(), any());
	}
}
