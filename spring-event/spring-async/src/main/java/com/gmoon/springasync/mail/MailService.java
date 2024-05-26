package com.gmoon.springasync.mail;

import java.util.concurrent.Future;
import java.util.function.Function;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springasync.member.Member;
import com.gmoon.springasync.member.MemberRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final JavaMailSender mailSender;
	private final MailTemplate mailTemplate;
	private final MemberRepository memberRepository;
	private MailService self;

	@PostConstruct
	public void init() {
		self = new MailService(mailSender, mailTemplate, memberRepository);
	}

	@Async
	@Transactional(readOnly = true)
	public void sendInviteMailFrom(final String publicUrl) {
		log.info("async send mail...");
		memberRepository.streamFindAll()
			 .map(Member::getEmail)
			 .filter(StringUtils::isNotBlank)
			 .map(convertInviteMailVO(publicUrl))
			 .forEach(this::sendMail);
	}

	@Async
	@Transactional(readOnly = true)
	public Future<String> sendInviteMailFromServerWillReturn(final String publicUrl) {
		self.sendInviteMailFrom(publicUrl);
		return new AsyncResult<>(publicUrl);
	}

	@Async
	@Transactional(readOnly = true)
	public void sendMailIfPublicUrlIsBlankThrow(final String publicUrl) {
		if (StringUtils.isBlank(publicUrl)) {
			throw new SendMailException();
		}

		self.sendInviteMailFrom(publicUrl);
	}

	@Async
	@Transactional(readOnly = true)
	public Future<String> sendMailWillReturnIfPublicUrlIsBlankThrow(final String publicUrl) {
		if (StringUtils.isBlank(publicUrl)) {
			throw new SendMailException();
		}

		return self.sendInviteMailFromServerWillReturn(publicUrl);
	}

	private void sendMail(InviteMailVO mailVO) {
		try {
			String email = mailVO.getReceiver();
			String inviteServerUrl = mailVO.getInviteServerUrl();
			SimpleMailMessage message = mailTemplate.createInviteGithubMailMessage(email, inviteServerUrl);
			mailSender.send(message);
			log.info("send mail success...");
		} catch (Exception e) {
			throw new SendMailException(e);
		}
	}

	private Function<String, InviteMailVO> convertInviteMailVO(String publicUrl) {
		return receiver -> InviteMailVO.createNew(receiver, publicUrl);
	}
}
