package com.gmoon.payment.global.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalErrorController implements ErrorController {

	@GetMapping("/error")
	public String handleError(HttpServletRequest request) throws Exception {
		throw (Exception)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
	}
}
