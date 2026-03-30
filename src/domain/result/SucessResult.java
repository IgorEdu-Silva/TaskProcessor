package domain.result;

import domain.model.TaskStatus;

public record SucessResult() implements TaskResult{
    @Override
    public TaskStatus resultingStatus() {
        return TaskStatus.DONE;
    }
}
