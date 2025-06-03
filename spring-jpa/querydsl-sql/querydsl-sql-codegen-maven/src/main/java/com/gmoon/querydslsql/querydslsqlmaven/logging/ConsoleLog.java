package com.gmoon.querydslsql.querydslsqlmaven.logging;

public class ConsoleLog {
	private final String label;

	public ConsoleLog(String label) {
		this.label = label;
	}

	public void info(String msg) {
		String message = formatMsg(Color.GREEN, "", msg);
		System.out.println(message);
	}

	public void warn(String msg) {
		String message = formatMsg(Color.YELLOW, "[WARN]", msg);
		System.out.println(message);
	}

	public void step(String msg) {
		String message = formatMsg(Color.BLUE, "", msg);
		System.out.println(message);
	}

	public void error(String msg) {
		String message = formatMsg(Color.RED, "[ERROR]", msg);
		System.err.println(message);
	}

	private String formatMsg(Color color, String prefix, String msg) {
		String format = String.format("[%s]%s %s", label, prefix, msg);
		return color.apply(format);
	}

	public void banner(String msg) {
		String bannerMsg = String.format("================== %s %s ==================", label, msg);
		System.out.println("\n" + Color.bold(Color.GREEN, bannerMsg) + "\n");
	}
}
