package com.taskprocessor.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "task.processor")
@Validated
public record TaskProcessorProperties(
        @Min(1)
        int maxConcurrency,
        @Min(1)
        int queueCapacity,
        @Min(1)
        int recoveryLimit
) {
}
