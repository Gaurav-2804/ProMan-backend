package com.data.proman.service;

import com.data.proman.enitity.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaskService {
    public List<Task> getAllTasks();

    public Task createTask(Task task);
}
