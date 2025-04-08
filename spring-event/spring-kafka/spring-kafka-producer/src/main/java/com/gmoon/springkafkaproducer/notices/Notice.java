package com.gmoon.springkafkaproducer.notices;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Notice {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private String id;

	private String userId;
	private String title;
	private String content;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	@Builder
	private Notice(String userId, String title, String content) {
		this.userId = userId;
		this.title = title;
		this.content = content;
	}

	public void updateContent(String content) {
		this.content = content;
	}
}
