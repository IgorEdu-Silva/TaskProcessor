package com.taskprocessor.infra.config;

import com.taskprocessor.application.config.TaskProcessorProperties;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.application.usecase.CreateTaskUseCase;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import com.taskprocessor.application.usecase.RecoverPendingTaskUseCase;
import com.taskprocessor.application.usecase.RetryTasksUseCase;
import com.taskprocessor.application.usecase.TimeoutTasksUseCase;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.service.TaskLifecycle;
import com.taskprocessor.infra.handler.DataProcessingHandler;
import com.taskprocessor.infra.handler.GenerateReportHandler;
import com.taskprocessor.infra.processor.AsyncTaskProcessor;
import com.taskprocessor.infra.repository.JdbcTaskRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.Map;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(TaskProcessorProperties.class)
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

    @Bean(destroyMethod = "close")
    public AsyncTaskProcessor processor(
            ProcessTaskUseCase processTaskUseCase,
            TaskProcessorProperties properties
    ) {
        return new AsyncTaskProcessor(
                processTaskUseCase,
                properties.maxConcurrency(),
                properties.queueCapacity()
        );
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
    public RecoverPendingTaskUseCase recoverPendingTaskUseCase(TaskRepositoryPort repository,
                                                               TaskProcessor processor) {
        return new RecoverPendingTaskUseCase(repository, processor, 1_000);
    }

    @Bean
    public TimeoutTasksUseCase timeoutTasksUseCase(TaskRepositoryPort repository,
                                                   TaskLifecycle lifecycle) {
        return new TimeoutTasksUseCase(repository, lifecycle, java.time.Duration.ofMinutes(5));
    }
}
