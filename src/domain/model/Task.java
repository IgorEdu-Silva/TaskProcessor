package domain.model;

import java.time.Instant;

public class Task {
    private Long id;
    private TaskType type;
    private TaskStatus status;
    private String payload;

    public Task(TaskType type, String payload){
        this.type = type;
        this.status = TaskStatus.PENDING;
        this.payload = payload;
    }

    public void startProcessing(){
        if (status != TaskStatus.PENDING) {
            throw new IllegalStateException(
              "Task cannot start from state" + status
            );
        }
        status = TaskStatus.PROCESSING;
    }

    public void markDone(){
        if (status != TaskStatus.PROCESSING){
            throw new IllegalStateException(
              "Task must be processing to finish"
            );
        }
        status = TaskStatus.DONE;
    }

    public void markError(){
        status = TaskStatus.ERROR;
    }

    public Long getId() {
        return id;
    }

    public TaskType getType() {
        return type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }
}
