package com.gmoon.springintegrationamqp.users.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springintegrationamqp.mail.model.SendMailVO;
import com.gmoon.springintegrationamqp.users.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserRestController {

	private final IntegrationFlow inSendMail;

	@PostMapping("/signup")
	@ResponseBody
	public ResponseEntity<Void> signup(@RequestBody User user) {
		sendWelcomeMail(user);
		return ResponseEntity.ok()
			.build();
	}

	private void sendWelcomeMail(User user) {
		inSendMail.getInputChannel().send(
			SendMailVO.builder()
				.to(user.getEmail())
				.from("system@email.com")
				.subject("hi " + user.getUsername())
				.content("welcome")
				.build()
				.toMessage()
		);
	}
}
