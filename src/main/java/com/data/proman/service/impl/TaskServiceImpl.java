package com.data.proman.service.impl;

import com.data.proman.enitity.Comment;
import com.data.proman.enitity.Member;
import com.data.proman.enitity.Project;
import com.data.proman.enitity.Task;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public void updateTask(String taskId, Task task) {
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent()) {
            taskRepository.save(task);
        }
        else {
            throw new EntityNotFoundException(null, Task.class);
        }
    }

}
