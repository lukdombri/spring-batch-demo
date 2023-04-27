package com.example.springbatchdemo.conf;

import com.example.springbatchdemo.dto.PersonDTO;
import com.example.springbatchdemo.listeners.JobCompletionNotificationListener;
import com.example.springbatchdemo.processors.PersonItemProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Date;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobRepository jobRepository;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;


    @Bean
    public FlatFileItemReader<PersonDTO> reader() {
        return new FlatFileItemReaderBuilder<PersonDTO>()
                .name("personItemReader")
                .resource(new ClassPathResource("test.csv"))
                .delimited()
                .names("firstName", "lastName")
                .fieldSetMapper(new RecordFieldSetMapper<>(PersonDTO.class))
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<PersonDTO> writer() {
        return new JdbcBatchItemWriterBuilder<PersonDTO>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO person (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    @Bean("importUserJob")
    public Job importUserJob(JobCompletionNotificationListener listener, Step step_first) {
        return new JobBuilder("importPersonsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step_first)
                .end()
                .build();
    }

    @Bean
    public Step step_first(JdbcBatchItemWriter<PersonDTO> writer) {
        return new StepBuilder("step_first", jobRepository)
                .<PersonDTO, PersonDTO>chunk(2, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public Step taskletStep() {
        return new StepBuilder("taskletStep", jobRepository)
                .tasklet(tasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet tasklet() {
        return (contribution, chunkContext) -> {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("random_score");
            jdbcCall.execute();
            return RepeatStatus.FINISHED;
        };
    }


    @Bean("callProcedureJob")
    public Job callProcedureJob(Step taskletStep) {
        return new JobBuilder("importPersonsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(taskletStep)
                .end()
                .build();
    }

}
