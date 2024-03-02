package com.data.proman.service.impl;

import com.data.proman.configurations.FireStoreConstants;
import com.data.proman.enitity.*;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.repository.TaskMemberMapRepository;
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

    @Autowired
    private TaskMemberMapRepository taskMemberMapRepository;

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
    public List<Task> getTasksByMember(String memberId) {
        Optional<TaskMemberMap> taskMemberMapEntity = taskMemberMapRepository.findById(memberId);
        if(taskMemberMapEntity.isPresent()) {
            TaskMemberMap taskMemberMap = taskMemberMapEntity.get();
            List<String> memberTaskIds = taskMemberMap.getStatusTasksMap().get("ASSIGNEE");
            return memberTaskIds.stream()
                    .map((taskId) -> {
                        Optional<Task> taskEntity = taskRepository.findById(taskId);
                        if(taskEntity.isPresent()) {
                            return taskEntity.get();
                        }
                        else {
                            throw new EntityNotFoundException(null, Task.class);
                        }
                    }).toList();
        }
        else {
            throw new EntityNotFoundException(404L, TaskMemberMap.class);
        }
    }

    @Override
    public void updateTask(String projectId, String taskId, Task task) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent() && projectEntity.isPresent()) {
            Project project = projectEntity.get();
            Task taskOld = taskEntity.get();
            if(!taskOld.getStatus().equals(task.getStatus())) {
                handleUpdateTaskProjectMap(projectId, taskId, task, project);
            }
            if(!taskOld.getAssigneeId().equals(task.getAssigneeId())){
                handleUpdateTaskMemberMap(task, taskOld.getAssigneeId(), "ASSIGNEE");
            }
            if(!taskOld.getReporterId().equals(task.getReporterId())) {
                handleUpdateTaskMemberMap(task, taskOld.getReporterId(), "REPORTER");
            }
            modelMapper.getConfiguration().setSkipNullEnabled(true);
            modelMapper.map(task, taskOld);
            taskRepository.save(taskOld);
        }
        else {
            throw new EntityNotFoundException(null, Task.class);
        }
    }

    private void handleUpdateTaskProjectMap(String projectId, String taskId, Task task, Project project) {
        Optional<TaskProjectMap> taskProjectMapEntity = taskProjectMapRepository.findById(projectId);
        if(taskProjectMapEntity.isPresent()) {
            TaskProjectMap taskProjectMap = taskProjectMapEntity.get();
            Map<String, List<String>> taskIdmaps = taskProjectMap.getTaskStatusMap();
            String taskStatus = task.getStatus();
            taskIdmaps.forEach((String status, List<String> idList) -> {
                if(status.equals(taskStatus)) {
                    taskIdmaps.get(status).add(taskId);
                }
                else {
                    taskIdmaps.get(status).remove(taskId);
                }
            });
            taskProjectMap.setTaskStatusMap(taskIdmaps);
            taskProjectMapRepository.save(taskProjectMap);
            configureProject(project, "UPDATE");
        } else {
            throw new EntityNotFoundException(null, TaskProjectMap.class);
        }
    }

    private void handleUpdateTaskMemberMap(Task task, String memberId, String memberType) {
        Optional<TaskMemberMap> oldMemberEntity = taskMemberMapRepository.findById(memberId);
        String taskId = task.getTaskId();
        if(oldMemberEntity.isPresent()) {
            TaskMemberMap oldMember = oldMemberEntity.get();
            Map<String, List<String>> taskIdmaps = oldMember.getStatusTasksMap();
            taskIdmaps.forEach((String memberStatus, List<String> idList) -> {
                if(memberStatus.equals(memberType)) {
                    taskIdmaps.get(memberStatus).remove(taskId);
                }
            });
            oldMember.setStatusTasksMap(taskIdmaps);
            taskMemberMapRepository.save(oldMember);
        }
        else {
            throw new EntityNotFoundException(404L, TaskMemberMap.class);
        }

        if(memberType.equals("ASSIGNEE")) {
            Optional<TaskMemberMap> newMemberEntity = taskMemberMapRepository.findById(task.getAssigneeId());
            TaskMemberMap memberMap;
            if(newMemberEntity.isPresent()) {
                memberMap = newMemberEntity.get();
                List<String> assigneeTaskIds = memberMap.getStatusTasksMap().get("ASSIGNEE");
                assigneeTaskIds.add(taskId);
                memberMap.getStatusTasksMap().put("ASSIGNEE", assigneeTaskIds);
            }
            else {
                memberMap = new TaskMemberMap();
                memberMap.setMemberId(memberId);
                memberMap.initializeStatusTasksMap();
                List<String> assigneeTaskIds = memberMap.getStatusTasksMap().get("ASSIGNEE");
                assigneeTaskIds.add(taskId);
                memberMap.getStatusTasksMap().put("ASSIGNEE", assigneeTaskIds);
            }
            taskMemberMapRepository.save(memberMap);
        }
        else if(memberType.equals("REPORTER")) {
            Optional<TaskMemberMap> newMemberEntity = taskMemberMapRepository.findById(task.getReporterId());
            TaskMemberMap memberMap;
            if(newMemberEntity.isPresent()) {
                memberMap = newMemberEntity.get();
                List<String> reporterTaskIds = memberMap.getStatusTasksMap().get("REPORTER");
                reporterTaskIds.add(taskId);
                memberMap.getStatusTasksMap().put("REPORTER", reporterTaskIds);
            }
            else {
                memberMap = new TaskMemberMap();
                memberMap.setMemberId(memberId);
                memberMap.initializeStatusTasksMap();
                List<String> reporterTaskIds = memberMap.getStatusTasksMap().get("REPORTER");
                reporterTaskIds.add(taskId);
                memberMap.getStatusTasksMap().put("REPORTER", reporterTaskIds);
            }
            taskMemberMapRepository.save(memberMap);
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
            configureTaskMemberMapping(taskId, task.getAssigneeId(), task.getReporterId());
            configureProject(project, "ADD");
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
            Task task = taskEntity.get();
            deleteTaskFromProjectMapping(projectId, taskId);
            deleteTaskFromMemberMapping(task.getAssigneeId(), task.getReporterId(), taskId);
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

    private void deleteTaskFromProjectMapping(String projectId, String taskId) {
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
            taskProjectMap.setTaskStatusMap(taskIdmaps);
            taskProjectMapRepository.save(taskProjectMap);
            configureProject(project, "REMOVE");
        }
    }

    private void deleteTaskFromMemberMapping(String assigneeId, String reporterId, String taskId) {
        Optional<TaskMemberMap> assigneeMapEntity = taskMemberMapRepository.findById(assigneeId);
        Optional<TaskMemberMap> reporterMapEntity = taskMemberMapRepository.findById(reporterId);
        if(assigneeMapEntity.isPresent()) {
            TaskMemberMap taskMemberMap = assigneeMapEntity.get();
            List<String> assigneeTaskIds = taskMemberMap.getStatusTasksMap().get("ASSIGNEE");
            assigneeTaskIds.remove(taskId);
            taskMemberMap.getStatusTasksMap().put("ASSIGNEE", assigneeTaskIds);
            taskMemberMapRepository.save(taskMemberMap);
        }
        if(reporterMapEntity.isPresent()) {
            TaskMemberMap taskMemberMap = reporterMapEntity.get();
            List<String> reporterTaskIds = taskMemberMap.getStatusTasksMap().get("REPORTER");
            reporterTaskIds.remove(taskId);
            taskMemberMap.getStatusTasksMap().put("REPORTER", reporterTaskIds);
            taskMemberMapRepository.save(taskMemberMap);
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

    private void configureTaskMemberMapping(String taskId, String assigneeId, String reporterId) {
        Optional<TaskMemberMap> assigneeMapEntity = taskMemberMapRepository.findById(assigneeId);
        Optional<TaskMemberMap> reporterMapEntity = taskMemberMapRepository.findById(reporterId);
        TaskMemberMap assigneeMap;
        TaskMemberMap reporterMap;

        if(assigneeMapEntity.isPresent()) {
            assigneeMap = assigneeMapEntity.get();
            List<String> taskIdsMap = assigneeMap.getStatusTasksMap().get("ASSIGNEE");
            taskIdsMap.add(taskId);
            assigneeMap.getStatusTasksMap().put("ASSIGNEE", taskIdsMap);
        }
        else {
            assigneeMap = new TaskMemberMap();
            assigneeMap.initializeStatusTasksMap();
            assigneeMap.setMemberId(assigneeId);
            List<String> taskIdsMap = assigneeMap.getStatusTasksMap().get("ASSIGNEE");
            taskIdsMap.add(taskId);
            assigneeMap.getStatusTasksMap().put("ASSIGNEE", taskIdsMap);
        }

        if(reporterMapEntity.isPresent()) {
            reporterMap = reporterMapEntity.get();
            List<String> taskIdsMap = reporterMap.getStatusTasksMap().get("REPORTER");
            taskIdsMap.add(taskId);
            reporterMap.getStatusTasksMap().put("REPORTER", taskIdsMap);
        }
        else {
            reporterMap = new TaskMemberMap();
            reporterMap.initializeStatusTasksMap();
            reporterMap.setMemberId(reporterId);
            List<String> taskIdsMap = reporterMap.getStatusTasksMap().get("REPORTER");
            taskIdsMap.add(taskId);
            reporterMap.getStatusTasksMap().put("REPORTER", taskIdsMap);
        }

        taskMemberMapRepository.save(assigneeMap);
        taskMemberMapRepository.save(reporterMap);
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

}
