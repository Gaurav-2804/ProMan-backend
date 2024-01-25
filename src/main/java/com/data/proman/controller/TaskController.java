package com.data.proman.controller;

import com.data.proman.enitity.Task;
import com.data.proman.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/api/getAllIssues")
    public List<Task> getIssues(){
        return taskService.getAllTasks();
    }

    @PostMapping("/api/createIssue")
    public Task createIssue(@RequestBody Task issue) {
        return taskService.createTask(issue);
    }

}
