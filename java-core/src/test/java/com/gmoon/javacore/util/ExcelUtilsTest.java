package com.gmoon.javacore.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;

class ExcelUtilsTest {

	@Test
	void download() {
		String resourceDirectoryPath = getResourceDirectoryPath();
		String storagePath = resourceDirectoryPath + "/storage";
		String fileName = "account";

		List<Account> accounts = Arrays.asList(
			new Account("toy", 10),
			new Account("gmoon", 20),
			new Account("kim", 30),
			new Account("lee", 30)
		);

		ExcelUtils.download(storagePath, fileName, accounts, Account.class);
	}

	private String getResourceDirectoryPath() {
		Path resourceDirectory = Paths.get("src", "test", "resources");
		return resourceDirectory.toFile().getAbsolutePath();
	}

	@RequiredArgsConstructor
	static class Account {
		private final String name;
		private final int age;
	}
}
