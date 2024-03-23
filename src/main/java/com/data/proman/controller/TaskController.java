package com.data.proman.controller;

import com.data.proman.enitity.Task;
import com.data.proman.enitity.dao.TaskDAO;
import com.data.proman.service.ProjectService;
import com.data.proman.service.TaskService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/api/getAllTasks")
    public ResponseEntity<List<Task>> getAllTasks(@RequestBody Map<String,String> payloadObject){
        List<Task> allTasksList = Collections.emptyList();
        if(payloadObject.containsKey("projectId")) {
            String projectId = payloadObject.get("projectId");
            allTasksList = taskService.getTasksInProject(projectId);
        }
        else if(payloadObject.containsKey("memberId")) {
            String memberId = payloadObject.get("memberId");
            allTasksList = taskService.getTasksByMember(memberId);
        }
        return new ResponseEntity<>(allTasksList, HttpStatus.OK);
    }

    @GetMapping("/api/{taskId}/getTask")
    public ResponseEntity<TaskDAO> getTask(@PathVariable String taskId) {
        TaskDAO taskRes =  taskService.getTask(taskId);
        return new ResponseEntity<>(taskRes, HttpStatus.OK);
    }

//    @GetMapping("/api/getAllTasks")
//    public ResponseEntity<List<Task>> getAllTasks(){
//        List<Task> allTasks = taskService.getAllTasks();
//        return new ResponseEntity<>(allTasks, HttpStatus.OK);
//    }

    @PostMapping("api/{projectId}/createTask")
    public ResponseEntity<Object> createTask(@RequestParam("files") MultipartFile[] files,
                                                          @RequestPart("taskDetails") @JsonDeserialize(as = Task.class) Task task,
                                                          @PathVariable String projectId) throws IOException {
        if(projectService.isExistingProject(projectId)) {
            String taskId = taskService.createTask(task, projectId, files);
            Map<String, Object> response = createResponseObject(taskId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Project with id " + projectId +" do not exist in the record.");
        }
    }

    @PostMapping("/api/uploadFiles")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("files") MultipartFile[] files,
                                                           @RequestPart("fileDetails") Map<String, String> fileInfo) throws IOException {
        String taskId = fileInfo.get("taskId");
        String projectKey = fileInfo.get("projectKey");
        Map<String,String> fileData = taskService.uploadFileData(taskId, projectKey, files);
        Map<String, Object> response = createResponseObject(taskId);
        return ResponseEntity.status(HttpStatus.CREATED).body(fileData);
    }

    @PostMapping("/api/{projectId}/{taskId}/deleteTask")
    public ResponseEntity<HttpStatus> deleteTask(@PathVariable String taskId,
                                                 @PathVariable String projectId) {
        taskService.deleteTask(projectId, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/api/{projectId}/{taskId}/updateTask")
    public ResponseEntity<HttpStatus> updateTask(@PathVariable String taskId,
                                                 @PathVariable String projectId, @RequestBody Task task) {
        taskService.updateTask(projectId, taskId, task);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Map<String, Object> createResponseObject(String taskId) {
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", "created");
        return  response;
    }

}
