package com.gmoon.springcloudbus.common.events;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonTypeName("UserLoginEvent")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserLoginEvent extends RemoteApplicationEvent {

	private String username;
	private String ipAddress;

	public UserLoginEvent(
		 Object source,
		 String originService,
		 String destinationService,
		 String username,
		 String ipAddress
	) {
		super(source, originService, DEFAULT_DESTINATION_FACTORY.getDestination(destinationService));
		this.username = username;
		this.ipAddress = ipAddress;
	}
}
