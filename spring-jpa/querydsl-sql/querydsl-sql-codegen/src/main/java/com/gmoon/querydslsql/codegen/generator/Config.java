package com.gmoon.querydslsql.codegen.generator;

import java.util.HashMap;
import java.util.Map;

public class Config {
	public final String jdbcDriver;
	public final String jdbcUrl;
	public final String jdbcUser;
	public final String jdbcPassword;
	public final String schema;
	public final String dialect;
	public final String basePackage;
	public final String targetFolder;
	public final String targetPackage;
	public final String namePrefix;

	public Config(String[] args) {
		Map<String, String> map = new HashMap<>();
		for (String arg : args) {
			if (arg.startsWith("--")) {
				String[] split = arg.substring(2).split("=", 2);
				if (split.length == 2) {
					map.put(split[0], split[1]);
				}
			}
		}
		this.jdbcDriver = map.get("jdbcDriver");
		this.jdbcUrl = map.get("jdbcUrl");
		this.jdbcUser = map.get("jdbcUser");
		this.jdbcPassword = map.get("jdbcPassword");
		this.schema = map.get("schema");
		this.dialect = map.get("dialect");
		this.namePrefix = map.get("namePrefix");
		this.basePackage = map.get("entityBasePackage");
		this.targetPackage = map.get("targetPackage");
		this.targetFolder = map.get("targetFolder");
	}

	@Override
	public String toString() {
		return "Config{" +
			 "jdbcDriver='" + jdbcDriver + '\'' +
			 ", jdbcUrl='" + jdbcUrl + '\'' +
			 ", jdbcUser='" + jdbcUser + '\'' +
			 ", jdbcPassword='" + jdbcPassword + '\'' +
			 ", schema='" + schema + '\'' +
			 ", dialect='" + dialect + '\'' +
			 ", entityBasePackage='" + basePackage + '\'' +
			 ", targetFolder='" + targetFolder + '\'' +
			 ", targetPackage='" + targetPackage + '\'' +
			 ", namePrefix='" + namePrefix + '\'' +
			 '}';
	}
}
