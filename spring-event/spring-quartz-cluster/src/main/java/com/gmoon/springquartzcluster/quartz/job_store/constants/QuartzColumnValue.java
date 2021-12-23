package com.gmoon.springquartzcluster.quartz.job_store.constants;

import com.gmoon.springquartzcluster.exception.NotSupportedInitializerException;

public final class QuartzColumnValue {
	public static final String ENABLED = "'1'";

	private QuartzColumnValue() {
		throw new NotSupportedInitializerException();
	}
}
