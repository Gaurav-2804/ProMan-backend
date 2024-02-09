package com.data.proman.controller;

import com.data.proman.enitity.Task;
import com.data.proman.enitity.TaskDAO;
import com.data.proman.service.TaskDAOService;
import com.data.proman.service.TaskService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDAOService taskDAOService;

    @GetMapping("/api/{projectKey}/getAllTasks")
    public List<Task> getTasks(@PathVariable String projectKey){
        return taskDAOService.getAllTasks(projectKey);
    }

//    @PostMapping("/api/{projectId}/createTask")
//    public ResponseEntity<HttpStatus> createTask(@RequestBody TaskDAO task, @PathVariable String projectId) {
//        taskDAOService.createTask(task, projectId);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }

    @PostMapping("api/{projectId}/createTask")
    public ResponseEntity<Map<String, Object>> createTask(@RequestParam("files") MultipartFile[] files,
                                                          @RequestPart("taskDetails") @JsonDeserialize(as = TaskDAO.class) TaskDAO task,
                                                          @PathVariable String projectId) throws IOException {
        String taskId = taskDAOService.createTask(task, projectId, files);
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", "created");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/{taskId}/updateTask")
    public ResponseEntity<HttpStatus> updateTask(@PathVariable String taskId, @RequestBody Task task) {
        taskService.updateTask(taskId, task);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
