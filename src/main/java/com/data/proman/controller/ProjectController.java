package com.data.proman.controller;

import com.data.proman.enitity.Project;
import com.data.proman.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ProjectController {

    @Autowired
    private ProjectService projectService;

        @GetMapping("/api/{memberId}/getAllProjects")
    public ResponseEntity<List<Project>> getAllProjects(@PathVariable String memberId){
        List<Project> projects = projectService.getAllProjects(memberId);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @PostMapping("/api/getProject")
    public ResponseEntity<Project> getProject(@RequestBody Map<String,String> payloadObject) {
        String projectId = payloadObject.get("projectId");
        Project project = projectService.getProject(projectId);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @PostMapping("/api/createProject")
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Project project) {
        String projectId = projectService.createProject(project);
        Map<String, Object> response = createResponseObject(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/updateProject")
    public ResponseEntity<Project> updateProject(@RequestBody Project project) {
        Project updatedProject = projectService.updateProject(project);
        return new ResponseEntity<>(updatedProject, HttpStatus.OK);
    }

    @PostMapping("/api/deleteProject")
    public ResponseEntity<HttpStatus> deleteProject(@RequestBody Map<String,String> payloadObject) {
        projectService.deleteProject(payloadObject.get("projectId"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Map<String, Object> createResponseObject(String projectId) {
        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        response.put("status", "created");
        return  response;
    }
}
