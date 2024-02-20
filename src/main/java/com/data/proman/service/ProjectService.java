package com.data.proman.service;

import com.data.proman.enitity.Project;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProjectService {
    public List<Project> getAllProjects();

    public String createProject(Project project);

    public Project getProject(String projectId);

    public Project updateProject(Project project);

    public void deleteProject(String projectId);
}
