package com.gmoon.springpoi.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gmoon.springpoi.SpringPoiApplication;
import com.gmoon.springpoi.excel.utils.ExcelUtil;
import com.gmoon.springpoi.users.domain.Role;
import com.gmoon.springpoi.users.domain.User;
import com.gmoon.springpoi.users.model.ExcelUserVO;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class ExcelSAXBenchmark {

	private static final String BASE_DIR = "src/main/resources/sample/";

	private ApplicationContext ctx;

	// @Param({"1", "100"})
	@Param({"1"})
	public int planCacheSize;

	@Setup(Level.Trial)
	public void setup() {
		if (ctx == null) {
			ctx = SpringApplication.run(SpringPoiApplication.class, "--server.port=9000");
		}

		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}

	@Benchmark
	public void readSax() throws IOException {
		String filename = String.format("sample-%d.xlsx", planCacheSize);
		ExcelUtil.readSAX(
			 Files.newInputStream(Paths.get(BASE_DIR, filename)),
			 ctx,
			 ExcelUserVO.class
		);
	}

	@Benchmark
	public void readDom() {
		String filename = String.format("sample-%d.xlsx", planCacheSize);
		String filePath = BASE_DIR + filename;

		ExcelUtil.read(
			 ctx,
			 filePath,
			 ExcelUserVO.class
		);
	}
}
