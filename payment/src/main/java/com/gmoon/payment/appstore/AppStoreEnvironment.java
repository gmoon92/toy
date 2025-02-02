package com.gmoon.payment.appstore;

import com.apple.itunes.storekit.model.Environment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AppStoreEnvironment {
	SANDBOX(Environment.SANDBOX),
	PRODUCTION(Environment.PRODUCTION),
	XCODE(Environment.XCODE),
	LOCAL_TESTING(Environment.LOCAL_TESTING); // Used for unit testing

	public final Environment value;
}
