package com.gmoon.springpoi.benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gmoon.springpoi.SpringPoiApplication;
import com.gmoon.springpoi.excel.helper.ExcelHelper;
import com.gmoon.springpoi.users.domain.Role;
import com.gmoon.springpoi.users.domain.User;
import com.gmoon.springpoi.users.model.ExcelUserVO;

/**
 * <ul>
 *   <li>@BenchmarkMode: 측정 모드는 여러 개 지정 가능. {@link Mode#All} 사용 시 모든 모드를 측정</li>
 *   <li>@Warmup: 워밍업 반복 횟수</li>
 *   <li>@Measurement: 측정 반복 횟수</li>
 *   <li>@Fork: 독립 JVM 실행 횟수(1이면 한 번만 실행)</li>
 *   <li>@Param: 파라미터별 벤치마크 실행 (예: 파일 크기, 로우 수 등 가변 조건 테스트)</li>
 * </ul>
 *
 * @see <a href="https://github.com/openjdk/jmh">github -openjdk jmh</a>
 * @see <a href="https://www.baeldung.com/java-microbenchmark-harness">baeldung -jmh</a>
 * @see <a href="https://medium.com/@python-javascript-php-html-css/managing-memory-accumulation-in-jmh-benchmarks-effectively-a6615a8f1007">Managing Memory Accumulation in JMH Benchmarks Effectively</a>
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
public class ExcelSAXBenchmark {
	private ApplicationContext ctx;
	private ExcelHelper helper;

	@Param({
		 "1",
		 "100",
		 "500",
		 "1000",
		 "5000",
		 "10000",
		 "15000",
		 "100000",
		 "150000"
	})
	public int excelDataRowSize;

	@Setup(Level.Trial)
	public void setup() {
		if (ctx == null) {
			ConfigurableApplicationContext context = SpringApplication.run(
				 SpringPoiApplication.class,
				 "--server.port=9000",
				 "--logging.level.root=WARN",
				 "--spring.main.banner-mode=off"
			);
			helper = context.getBean(ExcelHelper.class);
		}

		SecurityContext context = SecurityContextHolder.getContext();
		User user = new User("admin", null, Role.ADMIN);
		context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}

	@TearDown(Level.Trial)
	public void tearDown() {
		if (ctx != null) {
			SpringApplication.exit(ctx);
			ctx = null;
		}
	}

	@Benchmark
	public void readSax() throws IOException {
		helper.readSAX(
			 Files.newInputStream(Paths.get(getFilePath())),
			 ExcelUserVO.class
		);
	}

	@Benchmark
	public void readDom() {
		helper.read(
			 getFilePath(),
			 ExcelUserVO.class
		);
	}

	private String getFilePath() {
		String filepath = String.format("%s/%s/%s",
			 System.getProperty("user.dir"),
			 "src/test/resources/benchmark/excel/",
			 String.format("benchmark-%d.xlsx", excelDataRowSize)
		);
		return new File(filepath)
			 .getAbsolutePath();
	}
}
