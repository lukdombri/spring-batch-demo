package com.example.springbatchdemo.conf;

import com.example.springbatchdemo.dto.PersonDTO;
import com.example.springbatchdemo.listeners.JobCompletionNotificationListener;
import com.example.springbatchdemo.processors.PersonItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

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
    public JdbcBatchItemWriter<PersonDTO> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PersonDTO>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO person (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository,
                             JobCompletionNotificationListener listener, Step step_first) {
        return new JobBuilder("importPersonsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step_first)
                .end()
                .build();
    }

    @Bean
    public Step step_first(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager, JdbcBatchItemWriter<PersonDTO> writer) {
        return new StepBuilder("step_first", jobRepository)
                .<PersonDTO, PersonDTO>chunk(5, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
//                .faultTolerant()
//                .skipLimit(1)
//                .skip(IllegalArgumentException.class)
                .build();
    }
}
