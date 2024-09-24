package com.gmoon.springbatch.global;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class StudentItem implements Serializable {

	@Serial
	private static final long serialVersionUID = -9146231969273726448L;

	private String emailAddress;
	private String name;
	private long createdTime;
}
