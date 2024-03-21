package com.data.proman.service.impl;

import com.data.proman.enitity.Member;
import com.data.proman.enitity.Project;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.MemberRepository;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.service.CounterService;
import com.data.proman.service.ProjectService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CounterService counterService;

    private final ModelMapper modelMapper;

    private final MongoTemplate mongoTemplate;

    public ProjectServiceImpl(ModelMapper modelMapper, MongoTemplate mongoTemplate) {
        this.modelMapper = modelMapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Project> getAllProjects(String memberId) {
        Optional<Member> memberEntity = memberRepository.findByMemberId(memberId);
        if(memberEntity.isPresent()) {
            Member member = memberEntity.get();
            List<String> projectIds = member.getProjectId();
            return projectIds.stream()
                    .map(projectId -> {
                        return fetchProject(projectId);
                    }).toList();
        }
        else {
            throw new EntityNotFoundException(404L, Member.class);
        }
//        return projectRepository.findAll();
    }

    @Override
    public String createProject(Project project) {
        String projectKey = configureProjectKey();
        project.setKey(projectKey);
        project.setProjectId(project.getName(), projectKey);
        Project newProject = projectRepository.save(project);
        return newProject.getProjectId();
    }

    private String configureProjectKey() {
        Long pId = counterService.generateId(mongoTemplate, "Projects");
        return "PR_" + pId;
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

    @Override
    public Boolean isExistingProject(String projectId) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        return  projectEntity.isPresent();
    }

    private Project fetchProject(String projectId) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if (projectEntity.isPresent())
            return projectEntity.get();
        else
            throw new EntityNotFoundException(404L, Project.class);
    }

}
