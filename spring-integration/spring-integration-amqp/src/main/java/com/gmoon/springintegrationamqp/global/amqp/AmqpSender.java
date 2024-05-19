package com.gmoon.springintegrationamqp.global.amqp;

import java.io.Serializable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.ChannelResolverUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AmqpSender {

	private final BeanFactory beanFactory;

	public void send(String routingKey, Serializable payload) {
		MessageChannel channel = obtainMessageChannel(routingKey);
		log.info("routingKey: {}, payload: {}, channel: {}", routingKey, payload, channel);

		channel.send(wrrapMessage(payload));
	}

	private MessageChannel obtainMessageChannel(String routingKey) {
		DestinationResolver<MessageChannel> channelResolver = ChannelResolverUtils.getChannelResolver(beanFactory);
		return channelResolver.resolveDestination(routingKey);
	}

	private Message<Serializable> wrrapMessage(Serializable payload) {
		return MessageBuilder
			 .withPayload(payload)
			 .build();
	}
}
