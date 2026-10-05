package com.dev.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final AutowiringSpringBeanJobFactory
            jobFactory;

    @Bean
    public SchedulerFactoryBeanCustomizer
    schedulerFactoryBeanCustomizer() {

        return schedulerFactoryBean ->
                schedulerFactoryBean.setJobFactory(
                        jobFactory
                );
    }
}