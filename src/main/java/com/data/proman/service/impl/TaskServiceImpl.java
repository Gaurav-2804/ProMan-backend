package com.data.proman.service.impl;

import com.data.proman.configurations.FireStoreConstants;
import com.data.proman.enitity.*;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.repository.TaskProjectMapRepository;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.CounterService;
import com.data.proman.service.TaskService;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private CounterService counterService;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskProjectMapRepository taskProjectMapRepository;

    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    public TaskServiceImpl(MongoTemplate mongoTemplate, ModelMapper modelMapper) {
        this.mongoTemplate = mongoTemplate;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getTasksInProject(String projectId) {
        Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
        if(taskProjectMapEntity.isPresent()) {
            TaskProjectMap taskProjectMap = taskProjectMapEntity.get();
            List<String> taskList = new ArrayList<>();
            List<String> openTasks = taskProjectMap.getTaskStatusMap().getOrDefault("OPEN", Collections.emptyList());
            List<String> inProgressTasks = taskProjectMap.getTaskStatusMap().getOrDefault("IN Progress", Collections.emptyList());
            List<String> completeTasks = taskProjectMap.getTaskStatusMap().getOrDefault("COMPLETE", Collections.emptyList());

            taskList.addAll(openTasks);
            taskList.addAll(inProgressTasks);
            taskList.addAll(completeTasks);

            return taskList.stream()
                    .map((taskId) -> {
                        Optional<Task> taskEntity = taskRepository.findById(taskId);
                        if(taskEntity.isPresent()){
                            return taskEntity.get();
                        }
                        else {
                            throw new EntityNotFoundException(null, Task.class);
                        }
                    }).toList();
        }
        else {
            throw new EntityNotFoundException(404L, TaskProjectMap.class);
        }
    }
    
    @Override
    public void updateTask(String projectId, String taskId, Task task) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent() && projectEntity.isPresent()) {
            Project project = projectEntity.get();
            handleUpdateTaskProjectMap(projectId, taskId, task, project);
            Task taskOld = taskEntity.get();
            modelMapper.getConfiguration().setSkipNullEnabled(true);
            modelMapper.map(task, taskOld);
            taskRepository.save(taskOld);
        }
        else {
            throw new EntityNotFoundException(null, Task.class);
        }
    }

    private void handleUpdateTaskProjectMap(String projectId, String taskId, Task task, Project project) {
        if(task.getStatus().equals("Complete")) {
            Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
            if(taskProjectMapEntity.isPresent()) {
                TaskProjectMap taskProjectMap = taskProjectMapEntity.get();
                Map<String, List<String>> taskIdmaps = taskProjectMap.getTaskStatusMap();
                taskIdmaps.forEach((String status, List<String> idList) -> {
                    if(status.equals("COMPLETE")) {
                        taskIdmaps.get(status).add(taskId);
                    }
                    else {
                        taskIdmaps.get(status).remove(taskId);
                    }
                });
                taskProjectMap.setTaskStatusMap(taskIdmaps);
                taskProjectMapRepository.save(taskProjectMap);
                configureProject(project, "UPDATE");
            }
            else {
                throw new EntityNotFoundException(null, TaskProjectMap.class);
            }
        }
    }

    @Override
    public String createTask(Task task, String projectId, MultipartFile[] files) throws IOException {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent()) {
            Project project = projectEntity.get();
            String projectKey = project.getKey();
            String taskId = configureTaskId(task, projectKey);
            configureTask(task,taskId,projectKey,files);
            configureTaskProjectMappping(taskId,projectId);
            configureProject(project, "ADD");
//            handleProjectProgress(projectKey);
            return taskId;
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    @Override
    public void deleteTask(String projectId, String taskId) {
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent()) {
            deleteTaskFromMapping(projectId, taskId);
            taskRepository.deleteById(taskId);
        }
        else {
            throw new EntityNotFoundException(404L, Task.class);
        }
    }

    @Override
    public Map<String,String> uploadFileData(String taskId, String projectKey, MultipartFile[] files) {
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if (taskEntity.isPresent()) {
            Task task = taskEntity.get();
            Map<String,String> existingFileMap = task.getFilesMapping();
            if(!Arrays.asList(files).isEmpty()) {
                Map<String,String> fileMap = uploadImage(taskId, projectKey, files);
                existingFileMap.putAll(fileMap);
                taskRepository.save(task);
            }
            return existingFileMap;
        }
        else {
            throw new EntityNotFoundException(404L, Task.class);
        }
    }

    private Map<String,String> uploadImage(String taskId, String projectKey, MultipartFile[] files) {
        String storageBucket = FireStoreConstants.storageBucket;
        Storage storage = StorageClient.getInstance().bucket().getStorage();
        Map<String,String> filesMap = new HashMap<>();
        Arrays.stream(files).parallel().forEach((file) -> {
            String fileName = file.getOriginalFilename();
            int dotIndex = fileName.lastIndexOf('.');
            String sanitizedFileName = "";
            if(dotIndex != -1) {
                sanitizedFileName = fileName.substring(0,dotIndex);
            }
            String filePath = projectKey+"/" + taskId + "/" + fileName;
            BlobId blobId = BlobId.of(storageBucket, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                    .build();
            try {
                Blob blob = storage.create(blobInfo, file.getBytes());
                filesMap.put(sanitizedFileName,blob.getMediaLink());
            } catch (IOException e) {
                filesMap.put(fileName, "Error: " + e.getMessage());
            }
        });

        return filesMap;
    }

    private void deleteTaskFromMapping(String projectId, String taskId) {
        Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(taskProjectMapEntity.isPresent() && projectEntity.isPresent()) {
            TaskProjectMap taskProjectMap = taskProjectMapEntity.get();
            Project project = projectEntity.get();
            Map<String, List<String>> taskIdmaps = taskProjectMap.getTaskStatusMap();
//            taskIdmaps.forEach((String status, List<String> idList) -> {
//                taskIdmaps.get(status).remove(taskId);
//            });
            for(String status: taskIdmaps.keySet()) {
                taskIdmaps.get(status).remove(taskId);
            }
//            taskProjectMap.setTaskStatusMap(taskIdmaps);
            taskProjectMapRepository.save(taskProjectMap);
            configureProject(project, "REMOVE");
        }
    }

    private String configureTaskId(Task task, String projectKey) {
        Long tId = counterService.generateId(mongoTemplate,"Tasks");
        return projectKey + "-" + tId;
    }

    private void configureTaskProjectMappping(String taskId, String projectId) {
        Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
        TaskProjectMap taskProjectMap;
        if(taskProjectMapEntity.isPresent()) {
            taskProjectMap = taskProjectMapEntity.get();
            taskProjectMap.initializeTaskStatusMap();
            taskProjectMap.setProjectId(projectId);
            List<String> taskMappings = taskProjectMap.getTaskStatusMap().get("OPEN");
            taskMappings.add(taskId);
            taskProjectMap.getTaskStatusMap().put("OPEN", taskMappings);
        }
        else {
            taskProjectMap = new TaskProjectMap();
            taskProjectMap.initializeTaskStatusMap();
            taskProjectMap.setProjectId(projectId);
            List<String> taskMappings = taskProjectMap.getTaskStatusMap().get("OPEN");
            taskMappings.add(taskId);
            taskProjectMap.getTaskStatusMap().put("OPEN", taskMappings);
        }
        taskProjectMapRepository.save(taskProjectMap);
    }

    private void configureProject(Project project, String action){
        Integer totalTaks;
        if(action.equals("ADD")) {
            totalTaks = project.getTotalTasks() + 1;
        }
        else if(action.equals("UPDATE")){
            totalTaks = project.getTotalTasks();
        }
        else {
            totalTaks = project.getTotalTasks() - 1;
        }
        project.setTotalTasks(totalTaks);
        project.setProgress(getProjectProgress(project.getProjectId(), totalTaks));
        projectRepository.save(project);
    }

    private void configureTask(Task task, String taskId, String projectKey, MultipartFile[] files) throws IOException {
        task.setTaskId(taskId);
        task.setStatus("OPEN");
        if(!Arrays.asList(files).isEmpty()) {
            Map<String,String> fileData = uploadImage(task.getTaskId(), projectKey, files);
            task.setFilesMapping(fileData);
        }
        taskRepository.save(task);
    }

//    private List<String> fileUpload(MultipartFile[] files, String projectKey, String taskId) throws IOException {
//        String storageBucket = FireStoreConstants.storageBucket;
//        Storage storage = StorageClient.getInstance().bucket().getStorage();
//        return Arrays.stream(files).map((file) -> {
//            String fileName = file.getOriginalFilename();
//            String filePath = projectKey+"/" + taskId + "/" + fileName;
//            BlobId blobId = BlobId.of(storageBucket, filePath);
//            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                    .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
//                    .build();
//            try {
//                Blob blob = storage.create(blobInfo, file.getBytes());
//                return blob.getMediaLink();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }).collect(Collectors.toList());
//    }

    private Long getProjectProgress(String projectId, Integer totalNumberOfTasks) {
        Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(taskProjectMapEntity.isPresent() && projectEntity.isPresent()) {
            long totalTasks = (long)totalNumberOfTasks;
            if(totalTasks == 0) return 0L;
            TaskProjectMap taskProjectMap = taskProjectMapEntity.get();
            long completeTasks = (long) taskProjectMap.getTaskStatusMap().get("COMPLETE").size();
            return (completeTasks/totalTasks) * 100;
        }
        else {
            throw new EntityNotFoundException(404L, TaskProjectMap.class);
        }
    }

//    private void handleProjectProgress(String projectKey) {
//        Optional<Project> projectEntity = projectRepository.findByKey(projectKey);
//        if(projectEntity.isPresent()){
//            Project project = projectEntity.get();
//            List<TaskProjectMap> tasksList = taskProjectMapRepository.findAllByProjectKey(projectKey);
//            Long completedTasksCount = tasksList.stream()
//                    .filter(task -> {
//                        Optional<Task> taskRefEntity = taskRepository.findById(task.getTaskId());
//                        if(taskRefEntity.isPresent()){
//                            Task taskRef = taskRefEntity.get();
//                            return taskRef.getStatus().equals("Complete");
//                        }
//                        else {
//                            throw new EntityNotFoundException(null, Task.class);
//                        }
//                    })
//                    .count();
//            Long totalTasksCount = (long) tasksList.size();
//            Long progress = (completedTasksCount/totalTasksCount) * 100;
//            project.setProgress(progress);
//            projectRepository.save(project);
//        }
//        else {
//            throw new EntityNotFoundException(null,Project.class);
//        }
//
//    }

}
