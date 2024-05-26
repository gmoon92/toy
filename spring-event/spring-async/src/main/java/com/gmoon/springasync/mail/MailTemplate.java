package com.gmoon.springasync.mail;

import java.io.File;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.gmoon.javacore.util.FileUtils;
import com.gmoon.javacore.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailTemplate {
	private final SimpleMailMessage template;

	public SimpleMailMessage createInviteGithubMailMessage(String to, String githubPageUrl) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(template.getFrom());
		message.setTo(to);
		message.setSubject(String.format(template.getSubject(), to));
		File file = FileUtils.getResourceFile("templates/invite.html");
		String html = FileUtils.convertFileToString(file);
		message.setText(StringUtils.replace(html, "#githubPageUrl", githubPageUrl));
		return message;
	}
}
