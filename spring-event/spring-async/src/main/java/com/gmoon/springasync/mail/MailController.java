package com.gmoon.springasync.mail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springasync.server.Server;
import com.gmoon.springasync.server.ServerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("mail")
public class MailController {
	private final MailService mailService;
	private final ServerService serverService;

	@PostMapping("/send/invite")
	public ResponseEntity<String> sendInviteMailFromMyGithub() {
		Server githubServer = Server.createGithubBlogServer();
		String githubServerUrl = serverService.getWebServerWithoutPortUrl(githubServer);

		mailService.sendInviteMailFrom(githubServerUrl);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
