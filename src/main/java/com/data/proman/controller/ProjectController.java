package com.data.proman.controller;

import com.data.proman.enitity.Project;
import com.data.proman.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/api/getAllProjects")
    public ResponseEntity<List<Project>> getAllProjects(){
        List<Project> projects = projectService.getAllProjects();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @PostMapping("/api/createProject")
    public ResponseEntity<HttpStatus> createProject(@RequestBody Project project) {
        projectService.createProject(project);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
