package hexlet.code.service;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;

public interface TaskService {
    Task createTask(TaskDto dto);

    Task updateTask(long id, TaskDto dto);

    Task getCurrentTaskById(long id);
}
