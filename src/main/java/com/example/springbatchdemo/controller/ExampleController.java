package com.example.springbatchdemo.controller;

import com.example.springbatchdemo.entity.Person;
import com.example.springbatchdemo.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class ExampleController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @GetMapping()
    public String getPerson() {
        try {
            jobLauncher.run(job,new JobParameters());
        } catch (Exception e) {
            log.info(e.getMessage());
            return "FAILED";
        }
        return "SUCCESS";
    }

}
