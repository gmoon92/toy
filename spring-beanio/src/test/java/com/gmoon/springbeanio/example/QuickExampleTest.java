package com.gmoon.springbeanio.example;

import com.gmoon.springbeanio.core.AutoCloseableBeanReader;
import com.gmoon.springbeanio.core.AutoCloseableBeanWriter;
import lombok.extern.slf4j.Slf4j;
import org.beanio.StreamFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 자세한 내용은 <a href="http://beanio.org/2.0/docs/reference/index.html">beanio</a> 참고하자.
 * */
@Slf4j
class QuickExampleTest {

	@Test
	void test() {
		List<UserContactDto> result = runBeanIOStream(
			 getFile("contacts.xml"),
			 getFile("input.csv"),
			 getFile("output.csv")
		);

		assertThat(result).isNotEmpty();
	}

	@DisplayName("유효성 검증")
	@Test
	void strict() {
		List<UserContactDto> result = runBeanIOStream(
			 getFile("contacts-strict.xml"),
			 getFile("input.csv"),
			 getFile("output.csv")
		);

		assertThat(result).isNotEmpty();
	}

	@Test
	void withAnnotation() {
		List<AnnotationUserContactDto> result = runBeanIOStream(
			 getFile("contacts-annotation.xml"),
			 getFile("input.csv"),
			 getFile("output.csv")
		);

		assertThat(result).isNotEmpty();
	}

	private <T> List<T> runBeanIOStream(File mappingXml, File readerFile, File writerFile) {
		StreamFactory factory = StreamFactory.newInstance();
		factory.load(mappingXml);
		String streamName = "contacts";

		try (
			 AutoCloseableBeanReader in = new AutoCloseableBeanReader(factory.createReader(streamName, readerFile));
			 AutoCloseableBeanWriter out = new AutoCloseableBeanWriter(factory.createWriter(streamName, writerFile))
		) {
			List<T> result = new ArrayList<>();
			Object record;
			while ((record = in.read()) != null) {
				String recordName = in.getRecordName();
				if ("header".equals(recordName)) {
					Map<String, Object> header = (Map<String, Object>) record;
					for (Map.Entry<String, Object> entry : header.entrySet()) {
						log.info("[header] {}: {}", entry.getKey(), entry.getValue());
					}
				} else if ("body".equals(recordName)) {
					T body = (T) record;
					log.info("[body] {}", record);
					result.add(body);
				} else if ("trailer".equals(recordName)) {
					Integer recordCount = (Integer) record;
					log.info("[trailer] {}: {}", recordName, recordCount);
				}

				out.write(record);
			}

			out.flush();
			return result;
		}

	}

	private File getFile(String filename) {
		return new File("src/test/resources/beanio/example/" + filename);
	}
}
