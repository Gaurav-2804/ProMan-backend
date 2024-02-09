package com.data.proman.service.impl;

import com.data.proman.configurations.FireStoreConstants;
import com.data.proman.enitity.Member;
import com.data.proman.enitity.Project;
import com.data.proman.enitity.Task;
import com.data.proman.enitity.TaskDAO;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.MemberRepository;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.repository.TaskDAORepository;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.TaskDAOService;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskDAOServiceImpl implements TaskDAOService {
    @Autowired
    private TaskDAORepository taskDAORepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private MemberRepository memberRepository;
//    @Autowired
    private final ModelMapper modelMapper;


    private final MongoTemplate mongoTemplate;
    public TaskDAOServiceImpl(MongoTemplate mongoTemplate, ModelMapper modelMapper) {
        this.mongoTemplate = mongoTemplate;
        this.modelMapper = modelMapper;
    }
    @Override
    public List<Task> getAllTasks(String projectKey) {
        List<TaskDAO> allTasks = taskDAORepository.findAllByProjectKey(projectKey);
        return allTasks.stream()
                .map(taskDao -> modelMapper.map(taskDao, Task.class))
                .collect(Collectors.toList());
    }

    @Override
    public String createTask(TaskDAO task, String projectId, MultipartFile[] files) throws IOException {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent()) {
            Project project = projectEntity.get();
            String projectKey = project.getKey();
            TaskDAO taskFinal = configureProjectKey(task, projectKey);
            taskDAORepository.save(taskFinal);
            configureTask(taskFinal, files);
            configureProjectTasks(project);
            return taskFinal.getTaskId();
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    private TaskDAO configureProjectKey(TaskDAO task, String projectKey) {
        task.generateId(mongoTemplate);
        task.setTaskId(projectKey);
        task.setProjectKey(projectKey);
        return task;
    }

    private void configureTask(TaskDAO task, MultipartFile[] files) throws IOException {
        Task taskDb = modelMapper.map(task, Task.class);
        Optional<Member> assigneeMemberEntity = memberRepository.findByMemberId(task.getAssigneeId());
        if(assigneeMemberEntity.isPresent()) {
            Member assigneeMember = assigneeMemberEntity.get();
            taskDb.setAssignee(assigneeMember);
        }
        else {
            throw new EntityNotFoundException(null, Member.class);
        }
        Optional<Member> reporterMemberEntity = memberRepository.findByMemberId(task.getReporterId());
        if(reporterMemberEntity.isPresent()) {
            Member reporterMember = reporterMemberEntity.get();
            taskDb.setReporter(reporterMember);
        }
        else {
            throw new EntityNotFoundException(null, Member.class);
        }
        taskDb.setStatus("Open");
        List<String> imgUrls = fileUpload(files, task.getProjectKey(), task.getTaskId());
        taskDb.setImgUrls(imgUrls);
        taskRepository.save(taskDb);
    }

    private void configureProjectTasks(Project project){
        Integer totalTasks = project.getTotalTasks() + 1;
        project.setTotalTasks(totalTasks);
        List<TaskDAO> tasksInProject = taskDAORepository.findAllByProjectKey(project.getKey());
        Long completedTasksCount = tasksInProject.stream()
                        .filter(task -> {
//                            task.getStatus().equals("Complete")
                            Optional<Task> taskRefEntity = taskRepository.findById(task.getTaskId());
                            if(taskRefEntity.isPresent()){
                                Task taskRef = taskRefEntity.get();
                                return taskRef.getStatus().equals("Complete");
                            }
                            else {
                                throw new EntityNotFoundException(null, Task.class);
                            }
                        })
                        .count();
        Long totalTasksCount = (long) tasksInProject.size();
        Long progress = (completedTasksCount/totalTasksCount) * 100;
        project.setProgress(progress);
        projectRepository.save(project);
    }

    private List<String> fileUpload(MultipartFile[] files, String projectKey, String taskId) throws IOException {
        String storageBucket = FireStoreConstants.storageBucket;
        Storage storage = StorageClient.getInstance().bucket().getStorage();
        return Arrays.stream(files).map((file) -> {
            String fileName = file.getOriginalFilename();
            String filePath = projectKey+"/" + taskId + "/" + fileName;
            BlobId blobId = BlobId.of(storageBucket, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                    .build();
            try {
                Blob blob = storage.create(blobInfo, file.getBytes());
                return blob.getMediaLink();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
