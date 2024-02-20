package com.data.proman.service;

import com.data.proman.enitity.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface TaskService {
    public List<Task> getAllTasks();

    public List<Task> getTasksInProject(String projectId);

    public void updateTask(String projectId, String taskId, Task task);

    public String createTask(Task task, String projectId, MultipartFile[] files) throws IOException;

    public void deleteTask(String projectId, String taskId);

    public Map<String, String> uploadImage(String taskId, String projectKey, MultipartFile[] files);
}
