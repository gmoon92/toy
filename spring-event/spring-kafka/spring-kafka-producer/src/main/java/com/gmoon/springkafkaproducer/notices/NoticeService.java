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
	}

	@Transactional
	public void increaseLike(String id) {
		Notice notice = noticeRepository.findById(id)
			 .orElseThrow(EntityNotFoundException::new);

		noticeRepository.increaseLikeCount(id);

		OutboxMessage message = OutboxMessage.builder()
			 .aggregateId(notice.getId())
			 .aggregateType("notice")
			 .eventType("like-increased")
			 .payload(notice)
			 .build();
		outBoxRepository.save(message);
	}
}
