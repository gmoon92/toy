package com.gmoon.springbatch.custom;

import com.gmoon.springbatch.global.StudentItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InMemoryStudentJobConfig {

	@Bean
	public Job inMemoryStudentJob(
		 JobRepository jobRepository,
		 @Qualifier("inMemoryStudentStep") Step inMemoryStudentStep
	) {
		return new JobBuilder("inMemoryStudentJob", jobRepository)
			 .incrementer(new RunIdIncrementer())
			 .flow(inMemoryStudentStep)
			 .end()
			 .build();
	}

	@Bean
	public Step inMemoryStudentStep(
		 JobRepository jobRepository,
		 PlatformTransactionManager transactionManager
	) {
		return new StepBuilder("inMemoryStudentStep", jobRepository)
			 .chunk(1, transactionManager)
			 .reader(new InMemoryStudentReader())
			 .processor(processor())
			 .writer(inMemoryStudentWriter())
			 .build();
	}

	ItemProcessor<StudentItem, StudentItem> processor() {
		return item -> {
			log.info("Processing student information: {}", item);
			return item;
		};
	}

	ItemWriter<StudentItem> inMemoryStudentWriter() {
		return chunk -> {
			log.info("Received the information of {} students", chunk.size());

			chunk.forEach(i -> log.debug("Received the information of a student: {}", i));
		};
	}
}
