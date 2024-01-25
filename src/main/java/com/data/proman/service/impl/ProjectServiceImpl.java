package com.data.proman.service.impl;

import com.data.proman.enitity.Project;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
}
