package com.gmoon.springbeanio.example;

import lombok.extern.slf4j.Slf4j;
import org.beanio.BeanReader;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

@Slf4j
class QuickExampleTest {

	@Test
	void test() {
		StreamFactory factory = StreamFactory.newInstance();
		factory.load(getFile("contacts.xml"));

		String streamName = "contacts";
		BeanReader in = factory.createReader(streamName, getFile("input.csv"));
		BeanWriter out = factory.createWriter(streamName, getFile("output.csv"));

		Object record = null;
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

		in.close();
		out.flush();
		out.close();
	}

	private File getFile(String filename) {
		return new File("src/test/resources/beanio/example/" + filename);
	}
}
