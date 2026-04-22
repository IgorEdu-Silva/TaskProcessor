package application.port;

import java.util.UUID;

public interface TaskProcessor {
    void enqueue(UUID taskId);
}
