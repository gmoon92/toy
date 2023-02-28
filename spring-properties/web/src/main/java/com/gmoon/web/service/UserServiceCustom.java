package com.gmoon.web.service;

import org.springframework.stereotype.Service;

import com.gmoon.core.service.UserServiceImpl;

@Service
public class UserServiceCustom extends UserServiceImpl {

	@Override
	public String getUsername() {
		return "web admin";
	}
}
