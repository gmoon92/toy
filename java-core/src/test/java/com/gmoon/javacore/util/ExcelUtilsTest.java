package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;

class ExcelUtilsTest {

	@Test
	void upload() {
		String storagePath = getStoragePath();
		String fileName = "account_2023-11-06";
		String filePath = String.format("%s/%s", storagePath, fileName);

		List<Account> accounts = Arrays.asList(
			new Account("toy", 10),
			new Account("gmoon", 20),
			new Account("kim", 30),
			new Account("lee", 30)
		);

		assertThatCode(() -> ExcelUtils.upload(filePath, accounts, Account.class))
			.doesNotThrowAnyException();
	}

	private String getStoragePath() {
		Path resourceDirectory = Paths.get("src", "test", "resources", "storage");
		return resourceDirectory.toFile().getAbsolutePath();
	}

	@RequiredArgsConstructor
	static class Account {
		private final String name;
		private final int age;
	}
}
