package com.gmoon.commons.commonsapachepoi.common.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @see <a href="https://github.com/nahsra/antisamy">antisamy GitHub</a>
 * @see <a href="https://github.com/nahsra/antisamy/tree/main/src/main/resources">policy antisamy*.xml files</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XssUtil {
	private static final Policy policy;

	static {
		ClassLoader classLoader = XssUtil.class.getClassLoader();
		try (InputStream in = classLoader.getResourceAsStream("antisamy/antisamy.xml")) {
			if (in == null) {
				throw new FileNotFoundException("antisamy/antisamy.xml not found on classpath!");
			}
			policy = Policy.getInstance(in);
		} catch (PolicyException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCleanHTML(String str) {
		try {
			AntiSamy as = new AntiSamy();
			return as.scan(str, policy).getCleanHTML();
		} catch (PolicyException | ScanException e) {
			throw new RuntimeException(e);
		}
	}
}
