package com.gmoon.querydslsql.querydslsqlmaven;

public class Config {
	private String jdbcDriver;
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private String schema;
	private String dialect;
	private String basePackage;

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getJdbcUser() {
		return jdbcUser;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public String getSchema() {
		String suffix = "_querydslsql";
		if (schema == null) {
			return suffix;
		}
		return schema + "_querydslsql";
	}

	public String getDialect() {
		return dialect;
	}

	public String getBasePackage() {
		return basePackage;
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
			 ", basePackage='" + basePackage + '\'' +
			 '}';
	}
}
