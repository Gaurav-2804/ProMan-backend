package com.data.proman.service.impl;

import com.data.proman.enitity.Project;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.service.ProjectService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    private final ModelMapper modelMapper;

    public ProjectServiceImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public String createProject(Project project) {
        project.setProjectId(project.getName(), project.getKey());
        Project newProject = projectRepository.save(project);
        return newProject.getProjectId();
    }

    @Override
    public Project getProject(String projectId) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent()) {
            return projectEntity.get();
        }
        else{
            throw new EntityNotFoundException(404L, Project.class);
        }
    }

    @Override
    public Project updateProject(Project project) {
        Optional<Project> existingProjectEntity = projectRepository.findById(project.getProjectId());
        if(existingProjectEntity.isPresent()) {
            Project existingProject = existingProjectEntity.get();
            modelMapper.getConfiguration().setSkipNullEnabled(true);
            modelMapper.map(project, existingProject);
            return projectRepository.save(existingProject);
        }
        else {
            throw new EntityNotFoundException(404L, Project.class);
        }
    }

    @Override
    public void deleteProject(String projectId) {
        projectRepository.deleteById(projectId);
    }
}
