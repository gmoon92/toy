package com.gmoon.springkafkaproducer.notices;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NoticeServiceTest {

	@Autowired
	private NoticeService service;

	@Test
	void create() {
		CreateNoticeVO vo = new CreateNoticeVO();
		vo.setTitle("title");
		vo.setContent("content");

		Assertions.assertThatCode(() -> service.create(vo))
			 .doesNotThrowAnyException();
	}

	@Test
	void updateContent() {
		CreateNoticeVO vo = new CreateNoticeVO();
		vo.setTitle("title");
		vo.setContent("content");

		Notice notice = service.create(vo);

		Assertions.assertThatCode(() -> service.updateContent(notice.getId(), "update-content"))
			 .doesNotThrowAnyException();
	}
}
