package com.gmoon.springquartzcluster.exception;

public class NotFoundInvokeMethodException extends RuntimeException {
	public NotFoundInvokeMethodException() {
		super("Not found invocable method.");
	}
}
