package com.gmoon.cacheinvalidation.test;

import org.springframework.test.context.ActiveProfilesResolver;

public class ProfileResolver implements ActiveProfilesResolver {

	@Override
	public String[] resolve(Class<?> testClass) {
		return Profiles.containersEnabled()
			 ? new String[] {Profiles.TEST, Profiles.TESTCONTAINERS}
			 : new String[] {Profiles.TEST};
	}
}
