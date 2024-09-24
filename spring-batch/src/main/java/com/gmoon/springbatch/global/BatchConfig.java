package com.gmoon.springbatch.global;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/***
 * <pre>
 * Spring Batch 는 {@link EnableBatchProcessing} 를 통해 활성화 할 수 있다.
 * 이외에도 {@link DefaultBatchConfiguration} 상속하여 사용자 설정에 맞게 배치 기능을 재정의할 수 있다.
 * 다만 @EnableBatchProcessing 어노테이션과 DefaultBatchConfiguration 를 함께 사용할 수 없다.
 * </pre>
 *
 * @see EnableBatchProcessing
 * @see DefaultBatchConfiguration
 * */
@Configuration
//@EnableBatchProcessing // batch enabled
@RequiredArgsConstructor
public class BatchConfig
	 extends DefaultBatchConfiguration {

	private final DataSource dataSource;

//	@Bean
//	public Job simpleJob(JobRepository jobRepository, Step readStep) {
//		return new JobBuilder("simpleJob", jobRepository)
//			 .start(readStep)
//			 .preventRestart() // job 실패시 다시 시작 하지 않도록 설정
//			 .build();
//	}
//
//	@Bean
//	public Step readStep(
//		 JobRepository jobRepository,
//		 PlatformTransactionManager transactionManager,
//		 ItemReader<StudentItem> reader
//	) {
//		StepBuilder simpleStep = new StepBuilder("simpleStep", jobRepository);
//		return simpleStep
//			 .chunk(10, transactionManager)
//			 .reader(reader)
//			 .build();
//	}
//
//	@Bean
//	public ItemReader<StudentItem> reader() {
//		return new JdbcCursorItemReaderBuilder<StudentItem>()
//			 .name("cursorItemReader")
//			 .dataSource(dataSource)
//			 .sql("select * from myitem")
//			 .rowMapper(new BeanPropertyRowMapper<>(StudentItem.class))
//			 .build();
//	}
}
