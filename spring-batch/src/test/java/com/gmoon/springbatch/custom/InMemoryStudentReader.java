package com.gmoon.springbatch.custom;

import com.gmoon.springbatch.global.StudentItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.List;

@Slf4j
public class InMemoryStudentReader implements ItemReader<StudentItem> {

	private List<StudentItem> studentData;
	private int nextStudentIndex;

	InMemoryStudentReader() {
		initialize();
	}

	private void initialize() {
		studentData = List.of(
			 new StudentItem(
				  "tony.tester@gmail.com",
				  "Tony Tester",
				  System.currentTimeMillis()
			 ),
			 new StudentItem(
				  "nick.newbie@gmail.com",
				  "Nick Newbie",
				  System.currentTimeMillis()
			 ),
			 new StudentItem(
				  "ian.intermediate@gmail.com",
				  "Ian Intermediate",
				  System.currentTimeMillis()
			 )
		);
		nextStudentIndex = 0;
	}

	@Override
	public StudentItem read() throws Exception {
		log.info("Reading the information of the next student");

		StudentItem nextStudent = null;

		if (nextStudentIndex < studentData.size()) {
			nextStudent = studentData.get(nextStudentIndex);
			nextStudentIndex++;
		}

		log.info("Found student: {}", nextStudent);
		return nextStudent;
	}
}
