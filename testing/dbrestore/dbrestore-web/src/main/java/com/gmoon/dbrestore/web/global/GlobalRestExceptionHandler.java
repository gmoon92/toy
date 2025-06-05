package com.gmoon.dbrestore.web.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @link {https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html}
 */
@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

	@ExceptionHandler(Exception.class)
	protected ResponseEntity handleServerException(Exception ex) {
		log.error("", ex);
		return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
