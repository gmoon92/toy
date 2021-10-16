package com.gmoon.springasync.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailTemplate {
	private final SimpleMailMessage template;

	public SimpleMailMessage createInviteGithubMailMessage(String to, String githubPageUrl) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(template.getFrom());
		message.setTo(to);
		message.setSubject(String.format(template.getSubject(), to));
		message.setText(String.format(template.getText(), githubPageUrl));
		return message;
	}
}
