package com.gmoon.springkafkaproducer.messages;

import com.gmoon.springkafkaproducer.common.ColumnLength;
import com.gmoon.springkafkaproducer.global.converter.JsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class OutboxMessage {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@Column(length = ColumnLength.ID, nullable = false)
	private String aggregateId;

	@Column(length = ColumnLength.TYPE, nullable = false)
	private String aggregateType;

	@Column(length = ColumnLength.TYPE, nullable = false)
	private String eventType;

	@Convert(converter = JsonConverter.class)
//	@JdbcTypeCode(SqlTypes.JSON)
	@Lob
	@Column(nullable = false)
	private Object payload;

	@CreationTimestamp
	private Instant createdAt;
}
