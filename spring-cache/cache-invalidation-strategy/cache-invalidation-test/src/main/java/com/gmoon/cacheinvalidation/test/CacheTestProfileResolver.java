package com.gmoon.cacheinvalidation.test;

import org.springframework.test.context.ActiveProfilesResolver;

public class CacheTestProfileResolver implements ActiveProfilesResolver {

	@Override
	public String[] resolve(Class<?> testClass) {
		return CacheTestProfiles.containersEnabled()
			 ? new String[] {CacheTestProfiles.TEST, CacheTestProfiles.TESTCONTAINERS}
			 : new String[] {CacheTestProfiles.TEST};
	}
}
