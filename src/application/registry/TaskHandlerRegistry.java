package application.registry;

import application.handler.TaskHandler;
import domain.model.TaskType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskHandlerRegistry {
    private final Map<TaskType, TaskHandler> handlerMap;

    public TaskHandlerRegistry(List<TaskHandler> handlerMap) {
        this.handlerMap = handlerMap.stream().collect(Collectors.toMap(TaskHandler::supports, handler -> handler));
    }

    public TaskHandler getHandler(TaskType type){
        TaskHandler handler = handlerMap.get(type);

        if (handler == null){
            throw new IllegalArgumentException("No handler for task type" + type);
        }

        return handler;
    }
}
