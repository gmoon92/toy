package com.gmoon.springasync.mail;

import com.gmoon.javacore.util.FileUtils;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
		File file = FileUtils.getResourceFile("templates/invite.html");
		String html = FileUtils.convertFileToString(file);
		message.setText(StringUtils.replace(html, "#githubPageUrl", githubPageUrl));
		return message;
	}
}
