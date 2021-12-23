package com.gmoon.springasync.mail;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public class InviteMailVO implements Serializable {
	private static final long serialVersionUID = -2985816605246044954L;

	private final String receiver;
	private final String inviteServerUrl;

	private InviteMailVO(String receiver, String inviteServerUrl) {
		checkReceiverEmailNotBlank(receiver);
		this.receiver = receiver;
		this.inviteServerUrl = inviteServerUrl;
	}

	private void checkReceiverEmailNotBlank(String receiver) {
		if (StringUtils.isBlank(receiver)) {
			throw new IllegalArgumentException("수신자의 이메일은 공백을 허용할 수 없습니다.");
		}
	}

	public static InviteMailVO createNew(String receiver, String inviteServerUrl) {
		return new InviteMailVO(receiver, inviteServerUrl);
	}
}
