package domain.result;

import domain.model.TaskStatus;

public record PendingResult() implements TaskResult {
    @Override
    public TaskStatus resultingStatus(){
        return TaskStatus.PENDING;
    }
}
