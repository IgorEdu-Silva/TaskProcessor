package com.taskprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "com.taskprocessor",
        "com.taskprocessor.domain",
        "com.taskprocessor.application",
        "com.taskprocessor.infra",
        "com.taskprocessor.entry.controller"
})
public class TaskProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskProcessorApplication.class, args);
    }
}
