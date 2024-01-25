package com.data.proman.service.impl;

import com.data.proman.enitity.Task;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
}
