package com.gmoon.springquartzcluster.exception;

@SuppressWarnings("serial")
public class NotSupportedInitializerException extends RuntimeException {
	public NotSupportedInitializerException() {
		super("This class cannot be instantiated.");
	}
}
