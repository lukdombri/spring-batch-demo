package com.example.springbatchdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chunk")
@Slf4j
public class RunChunkJobController {

    private final JobLauncher jobLauncher;
    private final Job job;

    public RunChunkJobController(JobLauncher jobLauncher, @Qualifier("importUserJob") Job job){
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @GetMapping()
    public String runJob() {
        try {
            jobLauncher.run(job,new JobParameters());
        } catch (Exception e) {
            log.info(e.getMessage());
            return "FAILED";
        }
        return "SUCCESS";
    }

}
