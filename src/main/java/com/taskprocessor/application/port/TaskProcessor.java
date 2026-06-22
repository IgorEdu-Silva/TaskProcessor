package com.taskprocessor.application.port;

import java.util.UUID;

public interface TaskProcessor {
    TaskDispatchResult enqueue(UUID taskId);
}
