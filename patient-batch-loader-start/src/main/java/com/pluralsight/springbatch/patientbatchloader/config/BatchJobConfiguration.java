package com.pluralsight.springbatch.patientbatchloader.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class BatchJobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry){
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Bean
    public Job job(Step step) throws Exception {

        return this.jobBuilderFactory
            .get(Constants.JOB_NAME)
            .validator(validator())
            .start(step)
            .build();

    }

    @Bean
    public JobParametersValidator validator() {
        return new JobParametersValidator() {
            @Override
            public void validate(JobParameters parameters) throws JobParametersInvalidException {
                String fileName = parameters.getString(Constants.JOB_PARAM_FILE_NAME);
                if (StringUtils.isBlank(fileName)) {
                    throw new JobParametersInvalidException(
                        "The patient-batch-loader.fileName parameter is required.");
                }
                try {
                    Path file = Paths.get(applicationProperties.getBatch().getInputPath() +
                        File.separator + fileName);
                    if (Files.notExists(file) || !Files.isReadable(file)) {
                        throw new Exception("File did not exist or was not readable");
                    }
                } catch (Exception e) {
                    throw new JobParametersInvalidException(
                        "The input path + patient-batch-loader.fileName parameter needs to " +
                            "be a valid file location.");
                }
            }
        };
    }


    @Bean
    public Step step() throws Exception {
        return this.stepBuilderFactory
            .get(Constants.STEP_NAME)
            .tasklet(new Tasklet(){
                @Override
                public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                    throws Exception{
                    System.err.println("hello world");
                    return RepeatStatus.FINISHED;
                }
            })
            .build();
    }

}
