package com.gmoon.springbeanio.example;

import com.gmoon.springbeanio.core.AutoCloseableBeanReader;
import com.gmoon.springbeanio.core.AutoCloseableBeanWriter;
import lombok.extern.slf4j.Slf4j;
import org.beanio.StreamFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

@Slf4j
class QuickExampleTest {

	@Test
	void test() {
		runBeanIOStream(
			 getFile("contacts.xml"),
			 getFile("input.csv"),
			 getFile("output.csv")
		);
	}

	@DisplayName("유효성 검증")
	@Test
	void strict() {
		runBeanIOStream(
			 getFile("contacts-strict.xml"),
			 getFile("input.csv"),
			 getFile("output.csv")
		);
	}

	private void runBeanIOStream(File mappingXml, File readerFile, File writerFile) {
		StreamFactory factory = StreamFactory.newInstance();
		factory.load(mappingXml);
		String streamName = "contacts";

		try (
			 AutoCloseableBeanReader in = new AutoCloseableBeanReader(factory.createReader(streamName, readerFile));
			 AutoCloseableBeanWriter out = new AutoCloseableBeanWriter(factory.createWriter(streamName, writerFile))
		) {
			Object record;
			while ((record = in.read()) != null) {
				String recordName = in.getRecordName();
				if ("header".equals(recordName)) {
					Map<String, Object> header = (Map<String, Object>) record;
					for (Map.Entry<String, Object> entry : header.entrySet()) {
						log.info("[header] {}: {}", entry.getKey(), entry.getValue());
					}
				} else if ("body".equals(recordName)) {
					UserContactDto userContactDto = (UserContactDto) record;
					log.info("[body] {}", userContactDto);
				} else if ("trailer".equals(recordName)) {
					Integer recordCount = (Integer) record;
					log.info("[trailer] {}: {}", recordName, recordCount);
				}

				out.write(record);
			}

			out.flush();
		}
	}

	private File getFile(String filename) {
		return new File("src/test/resources/beanio/example/" + filename);
	}
}
