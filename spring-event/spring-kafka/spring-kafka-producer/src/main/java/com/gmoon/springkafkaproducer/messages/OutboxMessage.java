package com.gmoon.springkafkaproducer.messages;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class OutboxMessage {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private String id;

	@Column(length = 255, nullable = false)
	private String aggregateType;

	@Column(length = 255, nullable = false)
	private String aggregateId;

	@Column(length = 255, nullable = false)
	private String type;

	@Column(length = 255, nullable = false)
	private String payload;

}
