package com.gmoon.springintegrationamqp.mail.model;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class SaveMailLogVO implements Serializable {

	private static final long serialVersionUID = 6872799083647790141L;

	private String from;
	private String to;
	private String subject;
	private String content;

	public static SaveMailLogVO of(SendMailVO sendMailVO) {
		SaveMailLogVO vo = new SaveMailLogVO();
		vo.from = sendMailVO.getFrom();
		vo.to = sendMailVO.getTo();
		vo.subject = sendMailVO.getSubject();
		vo.content = sendMailVO.getContent();
		return vo;
	}
}
