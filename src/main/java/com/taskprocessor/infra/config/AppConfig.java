package com.taskprocessor.infra.config;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.application.usecase.CreateTaskUseCase;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import com.taskprocessor.application.usecase.RetryTasksUseCase;
import com.taskprocessor.application.usecase.TimeoutTasksUseCase;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.service.TaskLifecycle;
import com.taskprocessor.infra.handler.DataProcessingHandler;
import com.taskprocessor.infra.handler.GenerateReportHandler;
import com.taskprocessor.infra.processor.AsyncTaskProcessor;
import com.taskprocessor.infra.repository.JdbcTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.Map;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RetryPolicy retryPolicy() {
        return new RetryPolicy();
    }

    @Bean
    public TaskLifecycle taskLifecycle(RetryPolicy retryPolicy, Clock clock) {
        return new TaskLifecycle(retryPolicy, clock);
    }

    @Bean
    public TaskRepositoryPort repository(JdbcTemplate jdbc) {
        return new JdbcTaskRepository(jdbc);
    }

    @Bean
    public TaskHandlerRegistry registry() {
        return new TaskHandlerRegistry(Map.of(
                TaskType.GENERATE_REPORT, new GenerateReportHandler(),
                TaskType.DATA_PROCESSING, new DataProcessingHandler()
        ));
    }

    @Bean
    public ProcessTaskUseCase processTaskUseCase(TaskRepositoryPort repository,
                                                 TaskHandlerRegistry registry,
                                                 TaskLifecycle lifecycle) {
        return new ProcessTaskUseCase(repository, registry, lifecycle);
    }

    @Bean
    public TaskProcessor processor(ProcessTaskUseCase processTaskUseCase) {
        return new AsyncTaskProcessor(processTaskUseCase);
    }

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskRepositoryPort repository,
                                               TaskProcessor processor,
                                               TaskLifecycle lifecycle) {
        return new CreateTaskUseCase(repository, processor, lifecycle);
    }

    @Bean
    public RetryTasksUseCase retryTasksUseCase(TaskRepositoryPort repository,
                                               TaskProcessor processor,
                                               TaskLifecycle lifecycle) {
        return new RetryTasksUseCase(repository, processor, lifecycle);
    }

    @Bean
    public TimeoutTasksUseCase timeoutTasksUseCase(TaskRepositoryPort repository,
                                                   TaskLifecycle lifecycle) {
        return new TimeoutTasksUseCase(repository, lifecycle, java.time.Duration.ofMinutes(5));
    }
}
