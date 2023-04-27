package com.example.springbatchdemo.listeners;

import com.example.springbatchdemo.dto.PersonDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        long jobId = jobExecution.getJobId();
        log.info("JobId: " + jobId);
        jobExecution.getExecutionContext().put("jobId", jobId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate.query("SELECT first_name, last_name FROM person",
                    (rs, row) -> new PersonDTO(
                            rs.getString(1),
                            rs.getString(2))
            ).forEach(p -> log.info("Found " + p + " in the database."));
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.info("!!! JOB NOT FINISHED!");
        }
    }
}
