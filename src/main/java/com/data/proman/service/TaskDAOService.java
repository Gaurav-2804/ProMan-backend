package com.data.proman.service;

import com.data.proman.enitity.Task;
import com.data.proman.enitity.TaskDAO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface TaskDAOService {
    public List<Task> getAllTasks(String projectKey);

    public void createTask(TaskDAO task, String projectId, MultipartFile[] files) throws IOException;

}
