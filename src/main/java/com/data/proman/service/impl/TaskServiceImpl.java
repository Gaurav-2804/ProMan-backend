package com.data.proman.service.impl;

import com.data.proman.enitity.*;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.repository.TaskDAORepository;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.TaskService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskDAORepository taskDAORepository;

    private final ModelMapper modelMapper;

    public TaskServiceImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public void updateTask(String taskId, Task task) {
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent()) {
            if(task.getStatus().equals("Complete")) {
                Optional<TaskDAO> taskDAOEntity = taskDAORepository.findById(task.getTaskId());
                if (taskDAOEntity.isPresent()){
                    TaskDAO taskDAO = taskDAOEntity.get();
                    String projectKey = taskDAO.getProjectKey();
                    handleProjectProgress(projectKey);
                }
                else {
                    throw new EntityNotFoundException(null, TaskDAO.class);
                }
            }
            Task taskNew = taskEntity.get();
            modelMapper.map(task, taskNew);
            taskRepository.save(taskNew);
        }
        else {
            throw new EntityNotFoundException(null, Task.class);
        }
    }

    private void handleProjectProgress(String projectKey) {
        Optional<Project> projectEntity = projectRepository.findByKey(projectKey);
        if(projectEntity.isPresent()){
            Project project = projectEntity.get();
            List<TaskDAO> tasksList = taskDAORepository.findAllByProjectKey(projectKey);
            Long completedTasksCount = tasksList.stream()
                    .filter(task -> {
                        Optional<Task> taskRefEntity = taskRepository.findById(task.getTaskId());
                        if(taskRefEntity.isPresent()){
                            Task taskRef = taskRefEntity.get();
                            return taskRef.getStatus().equals("Complete");
                        }
                        else {
                            throw new EntityNotFoundException(null, Task.class);
                        }
                    })
                    .count();
            Long totalTasksCount = (long) tasksList.size();
            Long progress = (completedTasksCount/totalTasksCount) * 100;
            project.setProgress(progress);
            projectRepository.save(project);
        }
        else {
            throw new EntityNotFoundException(null,Project.class);
        }

    }

}
