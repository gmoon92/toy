package com.gmoon.springkafkaproducer.messages;

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
	private String key;

	private String entityId;
	private String type;
}
