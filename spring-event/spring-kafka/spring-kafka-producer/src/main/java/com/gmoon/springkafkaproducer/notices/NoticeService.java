package com.gmoon.springkafkaproducer.notices;

import com.gmoon.springkafkaproducer.messages.OutBoxRepository;
import com.gmoon.springkafkaproducer.messages.OutboxMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final OutBoxRepository outBoxRepository;

	@Transactional
	public Notice create(CreateNoticeVO vo) {
		Notice notice = Notice.builder()
			 .userId("user-id")
			 .title(vo.getTitle())
			 .content(vo.getContent())
			 .build();

		return noticeRepository.save(notice);
	}

	@Transactional
	public void updateContent(String id, String content) {
		Notice notice = noticeRepository.findById(id)
			 .orElseThrow(EntityNotFoundException::new);

		notice.updateContent(content);

		OutboxMessage message = OutboxMessage.builder()
			 .entityId(notice.getId())
			 .type("updated-content")
			 .build();
		outBoxRepository.save(message);
	}
}
